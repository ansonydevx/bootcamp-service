package com.onclass.bootcamp.infrastructure.adapters.persistence.repository;

import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BootcampRepository extends ReactiveCrudRepository<BootcampEntity, Long> {

    Mono<BootcampEntity> findByNombre(String nombre);

    @Query("""
            SELECT * FROM bootcamps
            ORDER BY nombre
            LIMIT :size OFFSET :offset
            """)
    Flux<BootcampEntity> findAllPaged(int size, long offset);
}
