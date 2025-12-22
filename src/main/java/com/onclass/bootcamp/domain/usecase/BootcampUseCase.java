package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class BootcampUseCase implements BootcampServicePort {

    private final BootcampPersistencePort persistencePort;
    private final CapacidadQueryPort capacidadQueryPort;

    public BootcampUseCase(BootcampPersistencePort persistencePort, CapacidadQueryPort capacidadQueryPort) {
        this.persistencePort = persistencePort;
        this.capacidadQueryPort = capacidadQueryPort;
    }

    @Override
    public Mono<Bootcamp> registrar(Bootcamp bootcamp) {
        return validar(bootcamp)
                .then(Mono.defer(() ->
                        persistencePort.existsByNombre(bootcamp.nombre())
                                .flatMap(exists -> {
                                    if (Boolean.TRUE.equals(exists)) {
                                        return Mono.error(
                                                new BusinessException(TechnicalMessage.CAPACIDAD_DUPLICADA));
                                    }
                                    return capacidadQueryPort.existenCapacidades(
                                            bootcamp.capacidadIds());
                                })
                                .flatMap(existen -> {
                                    if (Boolean.FALSE.equals(existen)) {
                                        return Mono.error(new BusinessException(
                                                TechnicalMessage.TECNOLOGIAS_NO_EXISTEN));
                                    }
                                    return persistencePort.save(bootcamp);
                                })
                ));
    }

    @Override
    public Flux<BootcampListado> listar(int page, int size, String sortBy, String direction) {
        return persistencePort.findAll(page, size)
                .collectList()
                .flatMapMany(bootcamps -> {
                    if ("cantidad".equalsIgnoreCase(sortBy)) {
                        bootcamps.sort((b1, b2) -> {
                            int compare = Integer.compare(
                                    b1.capacidadIds().size(),
                                    b2.capacidadIds().size()
                            );
                            return direction.equalsIgnoreCase("desc")
                                    ? -compare
                                    : compare;
                        });
                    }

                    if ("nombre".equalsIgnoreCase(sortBy)
                            && direction.equalsIgnoreCase("desc")) {
                        bootcamps.sort(
                                (b1, b2) -> b2.nombre().compareToIgnoreCase(b1.nombre())
                        );
                    }

                    List<Long> capacidadIds = bootcamps.stream()
                            .flatMap(b -> b.capacidadIds().stream())
                            .distinct()
                            .toList();

                    return capacidadQueryPort.obtenerCapacidadesPorIds(capacidadIds)
                            .flatMapMany(capacidades -> {
                                var capacidadMap = capacidades.stream()
                                        .collect(Collectors.toMap(
                                                CapacidadListado::id,
                                                c -> c
                                        ));

                                return Flux.fromIterable(bootcamps)
                                        .map(bootcamp ->
                                                new BootcampListado(
                                                        bootcamp.id(),
                                                        bootcamp.nombre(),
                                                        bootcamp.capacidadIds().stream()
                                                                .map(capId -> capacidadMap.get(capId))
                                                                .toList()
                                                ));
                            });
                });
    }


    private Mono<Void> validar(Bootcamp b) {
        if (b.capacidadIds() == null || b.capacidadIds().isEmpty())
            return Mono.error(new BusinessException(
                    TechnicalMessage.MINIMO_TECNOLOGIAS));

        if (b.capacidadIds().size() > 4)
            return Mono.error(new BusinessException(
                    TechnicalMessage.MAXIMO_TECNOLOGIAS));

        if (new HashSet<>(b.capacidadIds()).size() != b.capacidadIds().size())
            return Mono.error(new BusinessException(
                    TechnicalMessage.TECNOLOGIAS_REPETIDAS));

        return Mono.empty();
    }
}
