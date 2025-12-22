package com.onclass.bootcamp.infrastructure.adapters.persistence.mapper;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BootcampEntityMapper {
    Bootcamp toModel(BootcampEntity entity);

    @Mapping(target = "id", ignore = true)
    BootcampEntity toEntity(Bootcamp bootcamp);
}
