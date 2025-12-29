package com.onclass.bootcamp.infrastructure.adapters.gateway;

import com.onclass.bootcamp.domain.spi.ReporteCommandPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.ReporteBootcampRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ReporteWebClientAdapter implements ReporteCommandPort {

    private final WebClient webClient;

    @Override
    public Mono<Void> registrar(ReporteBootcampRequest request) {

        return webClient.post()
                .uri("/reportes/bootcamps")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Void.class);
    }
}
