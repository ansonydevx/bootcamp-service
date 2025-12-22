package com.onclass.bootcamp.infrastructure.entrypoints.handler;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class BootcampHandler {

    private final BootcampServicePort bootcampServicePort;

    public Mono<ServerResponse> registrar(ServerRequest request) {
        return request.bodyToMono(BootcampRequest.class)
                .map(dto -> new Bootcamp(
                        null,
                        dto.nombre(),
                        dto.descripcion(),
                        dto.fechaLanzamiento(),
                        dto.duracion(),
                        dto.capacidadIds()))
                .flatMap(bootcampServicePort::registrar)
                .flatMap(b -> ServerResponse
                        .status(HttpStatus.CREATED)
                        .bodyValue(b));
    }
}
