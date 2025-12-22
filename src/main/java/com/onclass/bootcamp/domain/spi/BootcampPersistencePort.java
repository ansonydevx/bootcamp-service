package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.domain.model.Bootcamp;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampPersistencePort {

    Mono<Boolean> existsByNombre(String nombre);
    Mono<Bootcamp> save(Bootcamp bootcamp);
    Flux<Bootcamp> findAll(int page, int size);
}
