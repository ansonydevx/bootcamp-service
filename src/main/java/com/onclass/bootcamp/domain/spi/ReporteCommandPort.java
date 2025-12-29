package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.ReporteBootcampRequest;
import reactor.core.publisher.Mono;

public interface ReporteCommandPort {
    Mono<Void> registrar(ReporteBootcampRequest reporte);
}
