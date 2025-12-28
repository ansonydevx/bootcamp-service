package com.onclass.bootcamp.domain.api;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampListado;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampServicePort {
    
    Mono<Bootcamp> registrar(Bootcamp bootcamp);
    Flux<BootcampListado> listar(int page, int size, String sortBy, String direction);

    Mono<Void> eliminar(Long id);
}
