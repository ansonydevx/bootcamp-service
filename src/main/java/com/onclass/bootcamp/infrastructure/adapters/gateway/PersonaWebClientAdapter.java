package com.onclass.bootcamp.infrastructure.adapters.gateway;

import com.onclass.bootcamp.domain.spi.PersonaQueryPort;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.PersonaResumen;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class PersonaWebClientAdapter implements PersonaQueryPort {

    private final WebClient webClient;

    @Override
    public Flux<PersonaResumen> obtenerPersonasPorBootcamp(Long bootcampId) {
        return webClient.get()
                .uri("/personas/bootcamps/{id}", bootcampId)
                .retrieve()
                .bodyToFlux(PersonaResumen.class);
    }
}
