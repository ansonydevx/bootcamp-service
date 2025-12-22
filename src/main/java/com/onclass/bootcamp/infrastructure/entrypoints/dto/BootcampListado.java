package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.util.List;

public record BootcampListado(
        Long id,
        String nombre,
        List<CapacidadListado> capacidades
) {}
