package com.onclass.bootcamp.infrastructure.adapters.gateway;

import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadExistsRequest;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.IdsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
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

    @Override
    public Mono<Void> eliminarCapacidades(List<Long> ids) {
        if (ids.isEmpty()) {
            return Mono.empty();
        }

        return webClient.post()
                .uri("/capacidades/delete-by-ids")
                .bodyValue(new IdsRequest(ids))
                .exchangeToMono(response -> {
                    if (response.statusCode().is2xxSuccessful()) {
                        return Mono.empty();
                    }

                    return response.bodyToMono(String.class)
                            .defaultIfEmpty("Error en MS Capacidad")
                            .doOnNext(msg ->
                                    log.error("Error desde MS Capacidad: {}", msg)
                            )
                            .flatMap(msg ->
                                    Mono.error(new RuntimeException(msg))
                            );
                });
    }

    @Override
    public Mono<Integer> contarTecnologiasPorCapacidades(List<Long> capacidadIds) {
        return webClient.post()
                .uri("/capacidades/contar-tecnologias")
                .bodyValue(new IdsRequest(capacidadIds))
                .retrieve()
                .bodyToMono(Integer.class);
    }
}
