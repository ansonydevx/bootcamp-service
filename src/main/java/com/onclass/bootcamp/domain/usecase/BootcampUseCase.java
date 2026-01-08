package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.*;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
public class BootcampUseCase implements BootcampServicePort {

    private final BootcampPersistencePort persistencePort;
    private final CapacidadQueryPort capacidadQueryPort;
    private final ReporteCommandPort reporteCommandPort;
    private final ReporteQueryPort reporteQueryPort;
    private final PersonaQueryPort personaQueryPort;

    public BootcampUseCase(
            BootcampPersistencePort persistencePort,
            CapacidadQueryPort capacidadQueryPort,
            ReporteCommandPort reporteCommandPort,
            ReporteQueryPort reporteQueryPort,
            PersonaQueryPort personaQueryPort
    ) {
        this.persistencePort = persistencePort;
        this.capacidadQueryPort = capacidadQueryPort;
        this.reporteCommandPort = reporteCommandPort;
        this.reporteQueryPort = reporteQueryPort;
        this.personaQueryPort = personaQueryPort;
    }

    @Override
    public Mono<Bootcamp> registrar(Bootcamp bootcamp) {
        return validar(bootcamp)
                .flatMap(this::verificarDuplicidad)
                .flatMap(this::verificarCapacidadesExisten)
                .flatMap(persistencePort::save)
                .doOnSuccess(this::publicarReporteAsync);
    }

    @Override
    public Flux<BootcampListado> listar(int page, int size, String sortBy, String direction) {
        return persistencePort.findAll(page, size)
                .collectList()
                .flatMapMany(bootcamps -> {
                    ordenar(bootcamps, sortBy, direction);
                    return listarConCapacidades(bootcamps);
                });
    }

    @Override
    public Flux<BootcampResumen> obtenerPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Flux.empty();
        }

        return persistencePort.findAllByIdIn(ids)
                .map(bootcamp ->
                        new BootcampResumen(
                                bootcamp.id(),
                                bootcamp.fechaLanzamiento(),
                                bootcamp.duracion()
                        ));
    }

    @Override
    public Mono<BootcampDetalle> obtenerBootcampMasExitoso() {
        return reporteQueryPort.obtenerBootcampMasExitosoId()
                .flatMap(persistencePort::findById)
                .flatMap(this::armarDetalleBootcamp);
    }

    @Override
    public Mono<Void> eliminar(Long id) {
        return persistencePort.findById(id)
                .switchIfEmpty(
                        Mono.error(
                                new BusinessException(TechnicalMessage.INTERNAL_ERROR)))
                .flatMap(this::eliminarConOrfandad);
    }

    private Mono<Bootcamp> validar(Bootcamp b) {
        if (b.capacidadIds() == null || b.capacidadIds().isEmpty())
            return Mono.error(new BusinessException(TechnicalMessage.MINIMO_CAPACIDADES));

        if (b.capacidadIds().size() > 4)
            return Mono.error(new BusinessException(TechnicalMessage.MAXIMO_CAPACIDADES));

        if (new HashSet<>(b.capacidadIds()).size() != b.capacidadIds().size())
            return Mono.error(new BusinessException(TechnicalMessage.CAPACIDADES_REPETIDAS));

        return Mono.just(b);
    }

    private Mono<Bootcamp> verificarDuplicidad(Bootcamp b) {
        return persistencePort.existsByNombre(b.nombre())
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new BusinessException(TechnicalMessage.BOOTCAMP_DUPLICADO))
                        : Mono.just(b)
                );
    }

    private Mono<Bootcamp> verificarCapacidadesExisten(Bootcamp b) {
        return capacidadQueryPort.existenCapacidades(b.capacidadIds())
                .flatMap(existen -> Boolean.TRUE.equals(existen)
                        ? Mono.just(b)
                        : Mono.error(new BusinessException(TechnicalMessage.CAPACIDADES_NO_EXISTEN))
                );
    }

    private void ordenar(List<Bootcamp> bootcamps, String sortBy, String direction) {
        Comparator<Bootcamp> comparator;

        if ("cantidad".equalsIgnoreCase(sortBy)) {
            comparator = Comparator.comparingInt(b -> b.capacidadIds().size());
        } else {
            comparator = Comparator.comparing(
                    Bootcamp::nombre,
                    String.CASE_INSENSITIVE_ORDER
            );
        }

        if ("desc".equalsIgnoreCase(direction)) {
            comparator = comparator.reversed();
        }

        bootcamps.sort(comparator);
    }

    private Flux<BootcampListado> listarConCapacidades(List<Bootcamp> bootcamps) {
        List<Long> capacidadIds = bootcamps.stream()
                .flatMap(b -> b.capacidadIds().stream())
                .distinct()
                .toList();

        return capacidadQueryPort.obtenerCapacidadesPorIds(capacidadIds)
                .collectMap(CapacidadListado::id)
                .flatMapMany(capacidadMap ->
                        Flux.fromIterable(bootcamps)
                                .map(bootcamp -> new BootcampListado(
                                        bootcamp.id(),
                                        bootcamp.nombre(),
                                        bootcamp.capacidadIds().stream()
                                                .map(capacidadMap::get)
                                                .filter(Objects::nonNull)
                                                .toList()
                                ))
                );
    }

    private Mono<Void> eliminarConOrfandad(Bootcamp bootcamp) {
        return obtenerCapacidadesHuerfanas(bootcamp.capacidadIds())
                .flatMap(capacidadesHuerfanas -> {
                    Mono<Void> eliminarBootcamp =
                            persistencePort.eliminarRelaciones(bootcamp.id())
                                    .then(persistencePort.deleteById(bootcamp.id()));

                    return capacidadesHuerfanas.isEmpty()
                            ? eliminarBootcamp
                            : eliminarBootcamp
                                .then(capacidadQueryPort.eliminarCapacidades(capacidadesHuerfanas));
                });
    }

    private Mono<List<Long>> obtenerCapacidadesHuerfanas(List<Long> capacidadIds) {
        return Flux.fromIterable(capacidadIds)
                .flatMap(id ->
                        persistencePort.countBootcampsReferencingCapacidad(id)
                                .filter(count -> count <= 1)
                                .map(count -> id)
                )
                .collectList();
    }

    private void publicarReporteAsync(Bootcamp bootcamp) {
        capacidadQueryPort.contarTecnologiasPorCapacidades(bootcamp.capacidadIds())
                .flatMap(totalTecnologias ->
                        reporteCommandPort.registrar(
                                new ReporteBootcampRequest(
                                        bootcamp.id(),
                                        bootcamp.nombre(),
                                        bootcamp.descripcion(),
                                        bootcamp.fechaLanzamiento(),
                                        bootcamp.duracion(),
                                        bootcamp.capacidadIds().size(),
                                        totalTecnologias,
                                        0
                                )
                        )
                )
                .doOnError(e ->
                        log.warn("No se pudo registrar reporte del bootcamp {}", bootcamp.id(), e)
                )
                .subscribe();
    }

    private Mono<BootcampDetalle> armarDetalleBootcamp(Bootcamp bootcamp) {
        Mono<List<CapacidadListado>> capacidadesMono =
                capacidadQueryPort.obtenerCapacidadesPorIds(bootcamp.capacidadIds())
                        .map(c -> new CapacidadListado(
                                c.id(),
                                c.nombre(),
                                c.tecnologias()
                        ))
                        .collectList();

        Mono<List<PersonaResumen>> personasMono =
                personaQueryPort.obtenerPersonasPorBootcamp(bootcamp.id())
                        .collectList();

        return Mono.zip(capacidadesMono, personasMono)
                .map(tuple -> new BootcampDetalle(
                        bootcamp.id(),
                        bootcamp.nombre(),
                        bootcamp.descripcion(),
                        bootcamp.fechaLanzamiento(),
                        bootcamp.duracion(),
                        tuple.getT1(),
                        tuple.getT2()
                ));
    }
}
