package com.onclass.bootcamp.infrastructure.entrypoints.dto;

import java.util.List;

public record CapacidadListado(
        Long id,
        String nombre,
        List<TecnologiaResumen> tecnologias
) {}
