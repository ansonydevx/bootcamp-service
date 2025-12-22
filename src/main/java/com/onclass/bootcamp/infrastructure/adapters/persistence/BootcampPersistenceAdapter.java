package com.onclass.bootcamp.infrastructure.adapters.persistence;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.infrastructure.adapters.persistence.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampCapacidadRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class BootcampPersistenceAdapter implements BootcampPersistencePort {

    private final BootcampRepository bootcampRepository;
    private final BootcampCapacidadRepository bootcampCapacidadRepository;
    private final BootcampEntityMapper mapper;

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return bootcampRepository.findByNombre(nombre)
                .hasElement();
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return bootcampRepository.save(mapper.toEntity(bootcamp))
                .flatMap(saved ->
                        Flux.fromIterable(bootcamp.capacidadIds())
                                .flatMap(id ->
                                        bootcampCapacidadRepository.save(
                                                new BootcampCapacidadEntity(saved.getId(), id)))
                                .then(Mono.just(
                                        new Bootcamp(
                                                saved.getId(),
                                                saved.getNombre(),
                                                saved.getDescripcion(),
                                                saved.getFechaLanzamiento(),
                                                saved.getDuracion(),
                                                bootcamp.capacidadIds()))));
    }
}
