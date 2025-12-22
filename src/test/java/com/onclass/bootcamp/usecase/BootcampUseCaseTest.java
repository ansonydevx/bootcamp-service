package com.onclass.bootcamp.usecase;

import com.onclass.bootcamp.domain.enums.TechnicalMessage;
import com.onclass.bootcamp.domain.exceptions.BusinessException;
import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.BootcampPersistencePort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.domain.usecase.BootcampUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

import static org.mockito.Mockito.*;

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

    private Bootcamp generarBootcampConCapacidades(int cantidad) {
        return new Bootcamp(
                null,
                "Bootcamp Java",
                "Una descripcion",
                LocalDate.now(),
                8,
                LongStream.rangeClosed(1, cantidad)
                        .boxed()
                        .toList()
        );
    }

    @Test
    void deberiaFallarSiNoTieneCapacidades() {
        Bootcamp bootcamp = generarBootcampConCapacidades(0);

        StepVerifier.create(useCase.registrar(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof BusinessException &&
                                ((BusinessException) error)
                                        .getTechnicalMessage() == TechnicalMessage.MINIMO_TECNOLOGIAS)
                .verify();
    }

    @Test
    void deberiaFallarSiTieneMasDe4Capacidades() {
        Bootcamp bootcamp = generarBootcampConCapacidades(5);

        StepVerifier.create(useCase.registrar(bootcamp))
                .expectErrorMatches(error ->
                        error instanceof BusinessException &&
                                ((BusinessException) error)
                                        .getTechnicalMessage() == TechnicalMessage.MAXIMO_TECNOLOGIAS)
                .verify();
    }

    @Test
    void deberiaRegistrarBootcampCorrectamente() {
        Bootcamp bootcamp = generarBootcampConCapacidades(3);

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
}
