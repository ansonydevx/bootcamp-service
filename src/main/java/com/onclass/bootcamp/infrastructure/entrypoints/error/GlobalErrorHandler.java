package com.onclass.bootcamp.infrastructure.entrypoints.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

@RequiredArgsConstructor
@Configuration
@Order(-2)
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof BusinessException be) {
            return writeBusinessError(exchange, be);
        }

        return writeTechnicalError(
                exchange,
                TechnicalMessage.INTERNAL_ERROR
        );
    }

    private Mono<Void> writeBusinessError(ServerWebExchange exchange, BusinessException ex) {
        TechnicalMessage tm = ex.getTechnicalMessage();

        HttpStatus status = HttpStatus.valueOf(
                Integer.parseInt(tm.getCode())
        );

        ErrorResponse body = new ErrorResponse(
                tm.getCode(),
                tm.getMessage(),
                tm.getParam(),
                Instant.now(),
                exchange.getRequest().getPath().value()
        );

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {
            return exchange.getResponse()
                    .writeWith(Mono.just(
                            exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(objectMapper.writeValueAsBytes(body))
                    ));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<Void> writeTechnicalError(ServerWebExchange exchange, TechnicalMessage tm) {
        ErrorResponse body = new ErrorResponse(
                tm.getCode(),
                tm.getMessage(),
                tm.getParam(),
                Instant.now(),
                exchange.getRequest().getPath().value()
        );

        exchange.getResponse()
                .setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        exchange.getResponse().getHeaders()
                .setContentType(MediaType.APPLICATION_JSON);

        try {
            return exchange.getResponse()
                    .writeWith(Mono.just(
                            exchange.getResponse()
                                    .bufferFactory()
                                    .wrap(objectMapper.writeValueAsBytes(body))
                    ));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
