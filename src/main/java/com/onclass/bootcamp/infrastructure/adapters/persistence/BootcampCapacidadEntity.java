package com.onclass.bootcamp.infrastructure.adapters.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

@Table("bootcamps_capacidades")
@Getter
@Setter
@AllArgsConstructor
public class BootcampCapacidadEntity {
    private Long bootcampId;
    private Long capacidadId;
}
