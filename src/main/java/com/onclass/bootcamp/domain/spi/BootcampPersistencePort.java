package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Mono;

public interface BootcampPersistencePort {

    Mono<Boolean> existsByNombre(String nombre);
    Mono<Bootcamp> save(Bootcamp bootcamp);
}
