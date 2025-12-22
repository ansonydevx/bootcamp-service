package com.onclass.bootcamp.infrastructure.entrypoints.router;

import com.onclass.bootcamp.infrastructure.entrypoints.handler.BootcampHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class BootcampRouter {

    @Bean
    public RouterFunction<ServerResponse> bootcampRoutes(
            BootcampHandler handler) {
        return route(POST("/bootcamps"), handler::registrar);
    }
}
