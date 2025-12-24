package com.onclass.bootcamp.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bootcampApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bootcamp API")
                        .description("Gestionar bootcamps")
                        .version("1.0.0"));
    }
}
