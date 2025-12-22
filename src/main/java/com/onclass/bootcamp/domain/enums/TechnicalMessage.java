package com.onclass.bootcamp.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TechnicalMessage {

    TECNOLOGIA_CREADA("201", "Tecnología registrada correctamente", ""),
    CAPACIDAD_CREADA("201", "Capacidad registrada correctamente", ""),
    CAPACIDAD_DUPLICADA("400", "La capacidad ya existe", "nombre"),
    TECNOLOGIAS_NO_EXISTEN("400", "Tecnologias no existen",  "tecnologiaIds"),
    MINIMO_TECNOLOGIAS("400", "Minimo debes seleccionar 3 tecnologias",  "tecnologiaIds"),
    MAXIMO_TECNOLOGIAS("400", "Maximo debes seleccionar 20 tecnologias",  "tecnologiaIds"),
    TECNOLOGIAS_REPETIDAS("400", "Maximo debes seleccionar 20 tecnologias",  "tecnologiaIds"),
    NOMBRE_INVALIDO("400", "Nombre inválido", "nombre"),
    DESCRIPCION_INVALIDA("400", "Descripción inválida", "descripcion"),
    INTERNAL_ERROR("500", "Error interno", "");

    private final String code;
    private final String message;
    private final String param;
}
