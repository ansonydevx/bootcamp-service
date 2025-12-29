package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadConTecnologias;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface CapacidadQueryPort {
    Mono<Boolean> existenCapacidades(List<Long> capacidadIds);
    Flux<CapacidadListado> obtenerCapacidadesPorIds(List<Long> capacidadIds);

    Mono<Void> eliminarCapacidades(List<Long> ids);
    Mono<Integer> contarTecnologiasPorCapacidades(List<Long> capacidadIds);
}
