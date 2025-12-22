package com.onclass.bootcamp.infrastructure.adapters.persistence.repository;

import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampCapacidadEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface BootcampCapacidadRepository extends ReactiveCrudRepository<BootcampCapacidadEntity, Long> {
    Flux<BootcampCapacidadEntity> findByBootcampId(Long bootcampId);
}
