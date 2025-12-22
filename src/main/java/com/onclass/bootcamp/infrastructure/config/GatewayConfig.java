package com.onclass.bootcamp.infrastructure.config;

import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.infrastructure.adapters.gateway.CapacidadWebClientAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    CapacidadQueryPort capacidadQueryPort(WebClient tecnologiaWebClient) {
        return new CapacidadWebClientAdapter(tecnologiaWebClient);
    }

    @Bean
    WebClient tecnologiaWebClient(@Value("${services.capacidades.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
