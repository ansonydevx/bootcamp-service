package com.onclass.bootcamp.application.config;

import com.onclass.bootcamp.domain.api.BootcampServicePort;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.domain.usecase.BootcampUseCase;
import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampPersistenceAdapter;
import com.onclass.bootcamp.infrastructure.adapters.persistence.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampCapacidadRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.reactive.TransactionalOperator;

@Configuration
@RequiredArgsConstructor
public class UseCasesConfig {

    private final BootcampRepository bootcampRepository;
    private final BootcampCapacidadRepository bootcampCapacidadRepository;
    private final BootcampEntityMapper bootcampEntityMapper;
    private final TransactionalOperator transactionalOperator;

    @Bean
    public BootcampPersistencePort bootcampPersistencePort() {
        return new BootcampPersistenceAdapter(
                bootcampRepository, bootcampCapacidadRepository, bootcampEntityMapper);
    }

    @Bean
    public BootcampServicePort bootcampServicePort(
            BootcampPersistencePort bootcampPersistencePort,
            CapacidadQueryPort capacidadQueryPort
    ) {
        return new BootcampUseCase(bootcampPersistencePort, capacidadQueryPort, transactionalOperator);
    }
}
