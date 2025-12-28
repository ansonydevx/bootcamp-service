package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.BootcampServiceApplication;
import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class BootcampUseCase implements BootcampServicePort {

    private final BootcampPersistencePort persistencePort;
    private final CapacidadQueryPort capacidadQueryPort;
    private final TransactionalOperator tx;

    public BootcampUseCase(
            BootcampPersistencePort persistencePort,
            CapacidadQueryPort capacidadQueryPort,
            TransactionalOperator tx
    ) {
        this.persistencePort = persistencePort;
        this.capacidadQueryPort = capacidadQueryPort;
        this.tx = tx;
    }

    @Override
    public Mono<Bootcamp> registrar(Bootcamp bootcamp) {
        return validar(bootcamp)
                .flatMap(this::verificarDuplicidad)
                .flatMap(this::verificarCapacidadesExisten)
                .flatMap(persistencePort::save);

    }

    @Override
    public Flux<BootcampListado> listar(int page, int size, String sortBy, String direction) {
        return persistencePort.findAll(page, size)
                .collectList()
                .flatMapMany(bootcamps -> {
                    ordenar(bootcamps, sortBy, direction);
                    return mapearConCapacidades(bootcamps);
                });
    }

    @Override
    public Mono<Void> eliminar(Long id) {
        return persistencePort.findById(id)
                .switchIfEmpty(Mono.error(new BusinessException(TechnicalMessage.INTERNAL_ERROR)))
                .flatMap(this::eliminarConOrfandad);
    }

    private Mono<Bootcamp> validar(Bootcamp b) {
        if (b.capacidadIds() == null || b.capacidadIds().isEmpty())
            return Mono.error(new BusinessException(TechnicalMessage.MINIMO_TECNOLOGIAS));

        if (b.capacidadIds().size() > 4)
            return Mono.error(new BusinessException(TechnicalMessage.MAXIMO_TECNOLOGIAS));

        if (new HashSet<>(b.capacidadIds()).size() != b.capacidadIds().size())
            return Mono.error(new BusinessException(TechnicalMessage.TECNOLOGIAS_REPETIDAS));

        return Mono.just(b);
    }

    private Mono<Bootcamp> verificarDuplicidad(Bootcamp b) {
        return persistencePort.existsByNombre(b.nombre())
                .flatMap(exists -> Boolean.TRUE.equals(exists)
                        ? Mono.error(new BusinessException(TechnicalMessage.CAPACIDAD_DUPLICADA))
                        : Mono.just(b)
                );
    }

    private Mono<Bootcamp> verificarCapacidadesExisten(Bootcamp b) {
        return capacidadQueryPort.existenCapacidades(b.capacidadIds())
                .flatMap(existen -> Boolean.TRUE.equals(existen)
                        ? Mono.just(b)
                        : Mono.error(new BusinessException(TechnicalMessage.TECNOLOGIAS_NO_EXISTEN))
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

    private Flux<BootcampListado> mapearConCapacidades(List<Bootcamp> bootcamps) {
        List<Long> capacidadIds = bootcamps.stream()
                .flatMap(b -> b.capacidadIds().stream())
                .distinct()
                .toList();

        return capacidadQueryPort.obtenerCapacidadesPorIds(capacidadIds)
                .collectList()
                .flatMapMany(capacidadesList -> {
                    Map<Long, CapacidadListado> capacidadMap = capacidadesList.stream()
                            .collect(Collectors.toMap(CapacidadListado::id, c -> c));

                    return Flux.fromIterable(bootcamps)
                            .map(bootcamp -> new BootcampListado(
                                    bootcamp.id(),
                                    bootcamp.nombre(),
                                    bootcamp.capacidadIds().stream()
                                            .map(capacidadMap::get)
                                            .filter(Objects::nonNull)
                                            .toList()
                            ));
                });
    }

    private Mono<Void> eliminarConOrfandad(Bootcamp bootcamp) {
        return obtenerCapacidadesHuerfanas(bootcamp.capacidadIds())
                .flatMap(capacidadesHuerfanas -> {
                    Mono<Void> eliminarBootcamp =
                            persistencePort.eliminarRelaciones(bootcamp.id())
                                    .then(persistencePort.deleteById(bootcamp.id()))
                                    .as(tx::transactional);

                    if (capacidadesHuerfanas.isEmpty()) {
                        return eliminarBootcamp;
                    }

                    return eliminarBootcamp
                            .then(capacidadQueryPort.eliminarCapacidades(capacidadesHuerfanas));
//                        persistencePort.eliminarRelaciones(bootcamp.id())
//                                .then(persistencePort.deleteById(bootcamp.id()))
//                                .as(tx::transactional)
//                                .then(capacidadQueryPort.eliminarCapacidades(capacidadesHuerfanas))
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
}
