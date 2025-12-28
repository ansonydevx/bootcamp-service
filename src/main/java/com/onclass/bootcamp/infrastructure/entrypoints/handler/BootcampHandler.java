package com.onclass.bootcamp.infrastructure.entrypoints.handler;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
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

    public Mono<ServerResponse> listar(ServerRequest request) {
        int page = Integer.parseInt(request.queryParam("page").orElse("0"));
        int size = Integer.parseInt(request.queryParam("size").orElse("10"));
        String sortBy = request.queryParam("sortBy").orElse("nombre");
        String direction = request.queryParam("direction").orElse("asc");

        return ServerResponse.ok()
                .body(
                        bootcampServicePort.listar(page, size, sortBy, direction),
                        BootcampListado.class
                );
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        Long id = Long.parseLong(request.pathVariable("id"));

        return bootcampServicePort.eliminar(id)
                .then(ServerResponse.noContent().build())
                .doOnError(ex -> log.error("Error al eliminar bootcamp {}: {}", id, ex.getMessage(), ex))
                .onErrorResume(ex ->
                        ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .bodyValue(Map.of(
                                        "error", ex.getMessage(),
                                        "bootcampId", id
                                ))
                );
    }
}
