package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadConTecnologias;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CapacidadQueryPort {
    Mono<Boolean> existenCapacidades(List<Long> capacidadIds);
    Mono<List<CapacidadListado>> obtenerCapacidadesPorIds(List<Long> capacidadIds);
}
