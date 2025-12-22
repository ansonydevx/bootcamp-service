package com.onclass.bootcamp.domain.model;

import java.time.LocalDate;
import java.util.List;

public record Bootcamp(
        Long id,
        String nombre,
        String descripcion,
        LocalDate fechaLanzamiento,
        Integer duracion,
        List<Long> capacidadIds
) {}