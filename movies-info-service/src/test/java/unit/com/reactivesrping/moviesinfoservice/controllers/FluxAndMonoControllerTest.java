package com.reactivesrping.moviesinfoservice.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@AutoConfigureWebTestClient
@WebFluxTest(FluxAndMonoController.class)
class FluxAndMonoControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void flux1() {
        webTestClient.get()
            .uri("/flux")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Integer.class)
            .hasSize(4);
    }

    @Test
    void flux2() {
        final Flux<Integer> flux = webTestClient.get()
            .uri("/flux")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Integer.class)
            .getResponseBody();

        StepVerifier.create(flux)
            .expectNext(1, 2, 3, 4)
            .verifyComplete();
    }

    @Test
    void flux3() {
        webTestClient.get()
            .uri("/flux")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBodyList(Integer.class)
            .consumeWith(listEntityExchangeResult -> {
                final List<Integer> responseBody = listEntityExchangeResult.getResponseBody();
                assert Objects.requireNonNull(responseBody).size() == 4;
            });
    }

    @Test
    void mono() {
        webTestClient.get()
            .uri("/mono")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .expectBody(String.class)
            .consumeWith(stringEntityExchangeResult -> {
                final String responseBody = stringEntityExchangeResult.getResponseBody();
                 assertThat(responseBody)
                     .isNotEmpty()
                     .isEqualTo("Hello world Mono");
            });
    }

    @Test
    void stream() {
        final Flux<Long> flux = webTestClient.get()
            .uri("/stream")
            .exchange()
            .expectStatus()
            .is2xxSuccessful()
            .returnResult(Long.class)
            .getResponseBody();

        StepVerifier.create(flux)
            .expectNext(0L, 1L, 2L, 3L, 4L)
            .thenCancel()
            .verify();
    }
}