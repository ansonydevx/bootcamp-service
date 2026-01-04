package com.onclass.bootcamp.infrastructure.adapters.persistence.mapper;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.infrastructure.adapters.persistence.BootcampEntity;
import java.time.LocalDate;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-26T14:33:54-0500",
    comments = "version: 1.6.3, compiler: javac, environment: Java 17.0.17 (Homebrew)"
)
@Component
public class BootcampEntityMapperImpl implements BootcampEntityMapper {

    @Override
    public Bootcamp toModel(BootcampEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Long id = null;
        String nombre = null;
        String descripcion = null;
        LocalDate fechaLanzamiento = null;
        Integer duracion = null;

        id = entity.getId();
        nombre = entity.getNombre();
        descripcion = entity.getDescripcion();
        fechaLanzamiento = entity.getFechaLanzamiento();
        duracion = entity.getDuracion();

        List<Long> capacidadIds = null;

        Bootcamp bootcamp = new Bootcamp( id, nombre, descripcion, fechaLanzamiento, duracion, capacidadIds );

        return bootcamp;
    }

    @Override
    public BootcampEntity toEntity(Bootcamp bootcamp) {
        if ( bootcamp == null ) {
            return null;
        }

        BootcampEntity bootcampEntity = new BootcampEntity();

        bootcampEntity.setNombre( bootcamp.nombre() );
        bootcampEntity.setDescripcion( bootcamp.descripcion() );
        bootcampEntity.setFechaLanzamiento( bootcamp.fechaLanzamiento() );
        bootcampEntity.setDuracion( bootcamp.duracion() );

        return bootcampEntity;
    }
}
