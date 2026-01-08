package com.onclass.bootcamp.application.config;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.spi.*;
import com.onclass.bootcamp.domain.usecase.BootcampUseCase;
import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampPersistenceAdapter;
import com.onclass.bootcamp.infrastructure.adapters.persistence.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampCapacidadRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

    private final BootcampRepository bootcampRepository;
    private final BootcampCapacidadRepository bootcampCapacidadRepository;
    private final BootcampEntityMapper bootcampEntityMapper;

    @Bean
    public BootcampPersistencePort bootcampPersistencePort() {
        return new BootcampPersistenceAdapter(
                bootcampRepository, bootcampCapacidadRepository, bootcampEntityMapper);
    }

    @Bean
    public BootcampServicePort bootcampServicePort(
            BootcampPersistencePort bootcampPersistencePort,
            CapacidadQueryPort capacidadQueryPort,
            ReporteCommandPort reporteCommandPort,
            ReporteQueryPort reporteQueryPort,
            PersonaQueryPort personaQueryPort
    ) {
        return new BootcampUseCase(
                bootcampPersistencePort,
                capacidadQueryPort,
                reporteCommandPort,
                reporteQueryPort,
                personaQueryPort);
    }
}
