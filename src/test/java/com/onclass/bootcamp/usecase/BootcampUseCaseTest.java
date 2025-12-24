package com.onclass.bootcamp.usecase;

import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.domain.usecase.BootcampUseCase;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.LongStream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BootcampUseCaseTest {

    private BootcampPersistencePort persistencePort;
    private CapacidadQueryPort capacidadQueryPort;
    private BootcampUseCase useCase;

    @BeforeEach
    void setup() {
        persistencePort = Mockito.mock(BootcampPersistencePort.class);
        capacidadQueryPort = Mockito.mock(CapacidadQueryPort.class);
        useCase = new BootcampUseCase(
                persistencePort,
                capacidadQueryPort);
    }

    private Bootcamp generarBootcampConCapacidades(
            Long id,
            String nombre,
            int cantidadCapacidades
    ) {
        return new Bootcamp(
                id,
                nombre,
                "Descripcion " + nombre,
                LocalDate.now(),
                8,
                LongStream.rangeClosed(1, cantidadCapacidades)
                        .boxed()
                        .toList()
        );
    }

    @Test
    void deberiaFallarSiNoTieneCapacidades() {
        Bootcamp bootcamp = generarBootcampConCapacidades(
                1L,
                "Bootcamp Java",
                0
        );

        StepVerifier.create(useCase.registrar(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof BusinessException &&
                                ((BusinessException) error)
                                        .getTechnicalMessage() == TechnicalMessage.MINIMO_TECNOLOGIAS)
                .verify();
    }

    @Test
    void deberiaFallarSiTieneMasDe4Capacidades() {
        Bootcamp bootcamp = generarBootcampConCapacidades(
                1L,
                "Bootcamp Java",
                5
        );

        StepVerifier.create(useCase.registrar(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof BusinessException &&
                                ((BusinessException) error)
                                        .getTechnicalMessage() == TechnicalMessage.MAXIMO_TECNOLOGIAS)
                .verify();
    }

    @Test
    void deberiaRegistrarBootcampCorrectamente() {
        Bootcamp bootcamp = generarBootcampConCapacidades(
                1L,
                "Bootcamp Java",
                3
        );

        when(persistencePort.existsByNombre(bootcamp.nombre()))
                .thenReturn(Mono.just(false));

        when(capacidadQueryPort.existenCapacidades(bootcamp.capacidadIds()))
                .thenReturn(Mono.just(true));

        when(persistencePort.save(bootcamp))
                .thenReturn(Mono.just(
                        new Bootcamp(
                                1L,
                                bootcamp.nombre(),
                                bootcamp.descripcion(),
                                bootcamp.fechaLanzamiento(),
                                bootcamp.duracion(),
                                bootcamp.capacidadIds()
                        )));

        StepVerifier.create(useCase.registrar(bootcamp))
                .expectNextMatches(b -> b.id() != null)
                .verifyComplete();
    }

    @Test
    void deberiaListarBootcampsPaginados() {
        Bootcamp b1 = generarBootcampConCapacidades(1L, "Bootcamp A", 1);
        Bootcamp b2 = generarBootcampConCapacidades(2L, "Bootcamp b", 1);

        when(persistencePort.findAll(0, 2))
                .thenReturn(Flux.just(b1, b2));

        when(capacidadQueryPort.obtenerCapacidadesPorIds(any()))
                .thenReturn(Flux.fromIterable(List.of()));

        StepVerifier.create(useCase.listar(0, 2, "nombre", "asc"))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void deberiaOrdenarPorNombreAsc() {
        Bootcamp b1 = generarBootcampConCapacidades(1L, "Alfa", 1);
        Bootcamp b2 = generarBootcampConCapacidades(2L, "Beta", 1);

        when(persistencePort.findAll(0, 10))
                .thenReturn(Flux.just(b1, b2));

        when(capacidadQueryPort.obtenerCapacidadesPorIds(any()))
                .thenReturn(Flux.fromIterable(List.of()));

        StepVerifier.create(useCase.listar(0, 10, "nombre", "asc"))
                .assertNext(b -> assertEquals("Alfa", b.nombre()))
                .assertNext(b -> assertEquals("Beta", b.nombre()))
                .verifyComplete();
    }

    @Test
    void deberiaOrdenarPorNombreDesc() {
        Bootcamp b1 = generarBootcampConCapacidades(1L, "Alfa", 1);
        Bootcamp b2 = generarBootcampConCapacidades(2L, "Beta", 1);

        when(persistencePort.findAll(0, 10))
                .thenReturn(Flux.just(b1, b2));

        when(capacidadQueryPort.obtenerCapacidadesPorIds(any()))
                .thenReturn(Flux.fromIterable(List.of()));

        StepVerifier.create(useCase.listar(0, 10, "nombre", "desc"))
                .assertNext(b -> assertEquals("Beta", b.nombre()))
                .assertNext(b -> assertEquals("Alfa", b.nombre()))
                .verifyComplete();
    }

    @Test
    void deberiaOrdenarPorCantidadDeCapacidadesDesc() {
        Bootcamp b1 = generarBootcampConCapacidades(1L, "Alfa", 1);
        Bootcamp b2 = generarBootcampConCapacidades(2L, "Beta", 3);

        when(persistencePort.findAll(0, 10))
                .thenReturn(Flux.just(b1, b2));

        when(capacidadQueryPort.obtenerCapacidadesPorIds(any()))
                .thenReturn(Flux.just(
                    new CapacidadListado(1L, "Cap 1", List.of()),
                    new CapacidadListado(2L, "Cap 2", List.of()),
                    new CapacidadListado(3L, "Cap 3", List.of())
                ));

        StepVerifier.create(useCase.listar(0, 10, "cantidad", "desc"))
                .assertNext(b -> assertEquals(3, b.capacidades().size()))
                .assertNext(b -> assertEquals(1, b.capacidades().size()))
                .verifyComplete();
    }
}
