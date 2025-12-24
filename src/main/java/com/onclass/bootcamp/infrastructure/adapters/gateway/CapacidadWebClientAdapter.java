package com.onclass.bootcamp.infrastructure.adapters.gateway;

import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadExistsRequest;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.IdsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CapacidadWebClientAdapter implements CapacidadQueryPort {

    private final WebClient webClient;

    @Override
    public Mono<Boolean> existenCapacidades(List<Long> capacidadIds) {
        return webClient.post()
                .uri("/capacidades/exists")
                .bodyValue(new CapacidadExistsRequest(capacidadIds))
                .retrieve()
                .bodyToMono(Boolean.class);
    }

    @Override
    public Flux<CapacidadListado> obtenerCapacidadesPorIds(List<Long> ids) {
        return webClient.post()
                .uri("/capacidades/by-ids")
                .bodyValue(new IdsRequest(ids))
                .retrieve()
                .bodyToFlux(CapacidadListado.class);
    }
}
