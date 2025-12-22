package com.onclass.bootcamp.infrastructure.adapters.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Table("bootcamps")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BootcampEntity {

    @Id
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDate fechaLanzamiento;
    private Integer duracion;
}
