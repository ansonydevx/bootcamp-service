package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.time.LocalDate;
import java.util.List;

public record BootcampRequest(
        String nombre,
        String descripcion,
        LocalDate fechaLanzamiento,
        Integer duracion,
        List<Long> capacidadIds
) {}
