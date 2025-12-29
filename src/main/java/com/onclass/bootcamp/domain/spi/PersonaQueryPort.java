package com.onclass.bootcamp.domain.spi;

import com.onclass.bootcamp.infrastructure.entrypoints.dto.PersonaResumen;
import reactor.core.publisher.Flux;

public interface PersonaQueryPort {
    Flux<PersonaResumen> obtenerPersonasPorBootcamp(Long bootcampId);
}
