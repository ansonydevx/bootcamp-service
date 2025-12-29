package com.onclass.bootcamp.infrastructure.config;

import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.domain.spi.PersonaQueryPort;
import com.onclass.bootcamp.domain.spi.ReporteCommandPort;
import com.onclass.bootcamp.domain.spi.ReporteQueryPort;
import com.onclass.bootcamp.infrastructure.adapters.gateway.CapacidadWebClientAdapter;
import com.onclass.bootcamp.infrastructure.adapters.gateway.PersonaWebClientAdapter;
import com.onclass.bootcamp.infrastructure.adapters.gateway.ReporteWebClientAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    @Bean
    CapacidadQueryPort capacidadQueryPort(WebClient capacidadWebClient) {
        return new CapacidadWebClientAdapter(capacidadWebClient);
    }

    @Bean
    ReporteCommandPort reporteCommandPort(WebClient reporteWebClient) {
        return new ReporteWebClientAdapter(reporteWebClient);
    }

    @Bean
    PersonaQueryPort personaQueryPort(WebClient personaWebClient) {
        return new PersonaWebClientAdapter(personaWebClient);
    }

    @Bean
    WebClient capacidadWebClient(@Value("${services.capacidades.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    WebClient reporteWebClient(@Value("${services.reportes.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Bean
    WebClient personaWebClient(@Value("${services.personas.base-url}") String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}
