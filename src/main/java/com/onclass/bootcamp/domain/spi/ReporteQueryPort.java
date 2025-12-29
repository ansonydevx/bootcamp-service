package com.onclass.bootcamp.domain.spi;

import reactor.core.publisher.Mono;

public interface ReporteQueryPort {
    Mono<Long> obtenerBootcampMasExitosoId();
}
