package com.onclass.bootcamp.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    BOOTCAMP_DUPLICADO("400", "El Bootcamp ya existe", "nombre"),
    CAPACIDADES_NO_EXISTEN("400", "Capacidades no existen",  "capacidadIds"),
    MINIMO_CAPACIDADES("400", "Minimo debes seleccionar 1 capacidad",  "capacidadIds"),
    MAXIMO_CAPACIDADES("400", "Maximo debes seleccionar 4 capacidades",  "capacidadIds"),
    CAPACIDADES_REPETIDAS("400", "Alguna(s) capacidades estan repetidas",  "capacidadIds"),
    NOMBRE_INVALIDO("400", "Nombre inválido", "nombre"),
    DESCRIPCION_INVALIDA("400", "Descripción inválida", "descripcion"),
    INTERNAL_ERROR("500", "Error interno", "");

    private final String code;
    private final String message;
    private final String param;
}
