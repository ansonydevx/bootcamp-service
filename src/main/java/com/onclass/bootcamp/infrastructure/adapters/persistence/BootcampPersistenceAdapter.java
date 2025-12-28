package com.onclass.bootcamp.infrastructure.adapters.persistence;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.infrastructure.adapters.persistence.mapper.BootcampEntityMapper;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampCapacidadRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class BootcampPersistenceAdapter implements BootcampPersistencePort {

    private final BootcampRepository bootcampRepository;
    private final BootcampCapacidadRepository bootcampCapacidadRepository;
    private final BootcampEntityMapper mapper;

    @Override
    public Mono<Boolean> existsByNombre(String nombre) {
        return bootcampRepository.findByNombre(nombre)
                .hasElement();
    }

    @Override
    public Mono<Bootcamp> save(Bootcamp bootcamp) {
        return bootcampRepository.save(mapper.toEntity(bootcamp))
                .flatMap(saved ->
                        Flux.fromIterable(bootcamp.capacidadIds())
                                .flatMap(id ->
                                        bootcampCapacidadRepository.save(
                                                new BootcampCapacidadEntity(null, saved.getId(), id)))
                                .then(Mono.just(
                                        new Bootcamp(
                                                saved.getId(),
                                                saved.getNombre(),
                                                saved.getDescripcion(),
                                                saved.getFechaLanzamiento(),
                                                saved.getDuracion(),
                                                bootcamp.capacidadIds()))));
    }

    @Override
    public Flux<Bootcamp> findAll(int page, int size) {
        long offset = (long) page * size;

        return bootcampRepository.findAllPaged(size, offset)
                .flatMap(entity ->
                        bootcampCapacidadRepository
                                .findByBootcampId(entity.getId())
                                .map(BootcampCapacidadEntity::getCapacidadId)
                                .collectList()
                                .map(capacidadIds ->
                                        new Bootcamp(
                                                entity.getId(),
                                                entity.getNombre(),
                                                entity.getDescripcion(),
                                                entity.getFechaLanzamiento(),
                                                entity.getDuracion(),
                                                capacidadIds
                                        )));
    }

    @Override
    public Flux<Bootcamp> findAllByIdIn(List<Long> ids) {
        return bootcampRepository.findAllByIdIn(ids)
                .map(mapper::toModel);
    }

    @Override
    public Mono<Bootcamp> findById(Long id) {
        return bootcampRepository.findById(id)
                .flatMap(entity ->
                        bootcampCapacidadRepository
                                .findByBootcampId(id)
                                .map(BootcampCapacidadEntity::getCapacidadId)
                                .collectList()
                                .map(capacidadIds ->
                                        new Bootcamp(
                                                entity.getId(),
                                                entity.getNombre(),
                                                entity.getDescripcion(),
                                                entity.getFechaLanzamiento(),
                                                entity.getDuracion(),
                                                capacidadIds
                                        )));
    }

    @Override
    @Transactional
    public Mono<Void> eliminarRelaciones(Long bootcampId) {
        return bootcampCapacidadRepository.deleteByBootcampId(bootcampId);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return bootcampRepository.deleteById(id);
    }

    @Override
    public Mono<Long> countBootcampsReferencingCapacidad(Long capacidadId) {
        return bootcampCapacidadRepository.countByCapacidadId(capacidadId);
    }
}
