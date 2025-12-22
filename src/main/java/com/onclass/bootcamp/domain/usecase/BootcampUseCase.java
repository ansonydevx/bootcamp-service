package com.onclass.bootcamp.domain.usecase;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;

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
