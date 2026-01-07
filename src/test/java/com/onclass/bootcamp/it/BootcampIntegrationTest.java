package com.onclass.bootcamp.it;

import com.onclass.bootcamp.domain.model.Bootcamp;
import com.onclass.bootcamp.domain.spi.PersonaQueryPort;
import com.onclass.bootcamp.domain.spi.ReporteCommandPort;
import com.onclass.bootcamp.domain.spi.ReporteQueryPort;
import com.onclass.bootcamp.domain.spi.CapacidadQueryPort;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampCapacidadRepository;
import com.onclass.bootcamp.infrastructure.adapters.persistence.repository.BootcampRepository;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.BootcampRequest;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.CapacidadListado;
import com.onclass.bootcamp.infrastructure.entrypoints.dto.TecnologiaResumen;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class BootcampIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    BootcampRepository bootcampRepository;

    @Autowired
    BootcampCapacidadRepository bootcampCapacidadRepository;

    @MockitoBean
    CapacidadQueryPort capacidadQueryPort;

    @MockitoBean
    ReporteCommandPort reporteCommandPort;

    @MockitoBean
    PersonaQueryPort personaQueryPort;

    @MockitoBean
    ReporteQueryPort reporteQueryPort;

    @BeforeEach
    void setup() {
        bootcampCapacidadRepository.deleteAll().block();
        bootcampRepository.deleteAll().block();

        when(capacidadQueryPort.existenCapacidades(anyList()))
                .thenReturn(Mono.just(true));

        when(capacidadQueryPort.contarTecnologiasPorCapacidades(anyList()))
                .thenReturn(Mono.just(5));

        when(reporteCommandPort.registrar(any()))
                .thenReturn(Mono.empty());
    }

    @Test
    void registrarBootcamp_ok() {
        BootcampRequest request = new BootcampRequest(
                "Bootcamp Java",
                "Backend avanzado",
                LocalDate.now(),
                12,
                List.of(1L, 2L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isNumber()
                .jsonPath("$.nombre").isEqualTo("Bootcamp Java")
                .jsonPath("$.capacidadIds.length()").isEqualTo(2);

        StepVerifier.create(bootcampRepository.findByNombre("Bootcamp Java"))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(bootcampCapacidadRepository.findAll())
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void listarBootcamps_ok() {

        when(capacidadQueryPort.existenCapacidades(anyList()))
                .thenReturn(Mono.just(true));

        when(capacidadQueryPort.obtenerCapacidadesPorIds(anyList()))
                .thenReturn(Flux.just(
                        new CapacidadListado(
                                1L,
                                "Backend",
                                List.of(
                                        new TecnologiaResumen(1L, "Java"),
                                        new TecnologiaResumen(2L, "Spring")
                                )
                        )
                ));

        BootcampRequest request = new BootcampRequest(
                "Bootcamp Backend",
                "Desc",
                LocalDate.now(),
                12,
                List.of(1L)
        );

        webTestClient.post()
                .uri("/bootcamps")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/bootcamps")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .queryParam("sortBy", "nombre")
                        .queryParam("direction", "asc")
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].nombre").isEqualTo("Bootcamp Backend")
                .jsonPath("$[0].capacidades[0].nombre").isEqualTo("Backend")
                .jsonPath("$[0].capacidades[0].tecnologias.length()").isEqualTo(2);
    }

    @Test
    void eliminarBootcamp_conCapacidadesHuerfanas_ok() {

        when(capacidadQueryPort.eliminarCapacidades(anyList()))
                .thenReturn(Mono.empty());

        when(capacidadQueryPort.existenCapacidades(anyList()))
                .thenReturn(Mono.just(true));

        BootcampRequest request = new BootcampRequest(
                "Bootcamp Orfano",
                "Desc",
                LocalDate.now(),
                10,
                List.of(1L)
        );

        Long bootcampId =
                webTestClient.post()
                        .uri("/bootcamps")
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isCreated()
                        .returnResult(Bootcamp.class)
                        .getResponseBody()
                        .map(Bootcamp::id)
                        .blockFirst();

        Assertions.assertNotNull(bootcampId);
        StepVerifier.create(bootcampRepository.findById(bootcampId))
                .expectNextCount(1)
                .verifyComplete();

        webTestClient.delete()
                .uri("/bootcamps/{id}", bootcampId)
                .exchange()
                .expectStatus().isNoContent();

        StepVerifier.create(bootcampRepository.findById(bootcampId))
                .verifyComplete();

        StepVerifier.create(bootcampCapacidadRepository.findByBootcampId(bootcampId))
                .verifyComplete();

        verify(capacidadQueryPort)
                .eliminarCapacidades(List.of(1L));
    }

}
