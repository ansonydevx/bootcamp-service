package com.onclass.bootcamp.infrastructure.adapters.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("bootcamps_capacidades")
@Getter
@Setter
@AllArgsConstructor
public class BootcampCapacidadEntity {
    @Id
    private Long id;
    @Column("bootcamp_id")
    private Long bootcampId;
    @Column("capacidad_id")
    private Long capacidadId;
}
