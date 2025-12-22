package com.onclass.bootcamp.infrastructure.adapters.gateway;

import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadExistsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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
}
