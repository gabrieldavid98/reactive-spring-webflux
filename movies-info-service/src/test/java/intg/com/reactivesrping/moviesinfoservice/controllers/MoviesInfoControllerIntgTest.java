package com.reactivesrping.moviesinfoservice.controllers;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import com.reactivesrping.moviesinfoservice.repositories.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.method.support.UriComponentsContributor;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;


@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
class MoviesInfoControllerIntgTest {
    private static final String API_V1_MOVIES_INFO_PATH = "/api/v1/movies-info";

    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @Autowired
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        final var movieInfos = List.of(
            new MovieInfo(null, "Dark Knight Rises 1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
            new MovieInfo(null, "The Dark Knight",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );

        movieInfoRepository.saveAll(movieInfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void create() {
        final MovieInfo movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.post()
            .uri(API_V1_MOVIES_INFO_PATH)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                final MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                assertThat(savedMovieInfo)
                    .isNotNull()
                    .matches(it -> Objects.nonNull(it.getMovieInfoId()));
            });
    }

    @Test
    void index() {
        webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
    }

    @Test
    void indexStream() {
        final MovieInfo movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.post()
            .uri(API_V1_MOVIES_INFO_PATH)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                final MovieInfo savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                assertThat(savedMovieInfo)
                    .isNotNull()
                    .matches(it -> Objects.nonNull(it.getMovieInfoId()));
            });

        final Flux<MovieInfo> movieInfoFlux = webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH + "/stream")
            .exchange()
            .expectStatus().isOk()
            .returnResult(MovieInfo.class)
            .getResponseBody();

        StepVerifier.create(movieInfoFlux)
            .assertNext(movieInfo1 -> {
                assertThat(movieInfo1.getMovieInfoId()).isNotNull();
            })
            .thenCancel()
            .verify();
    }

    @Test
    void indexByYear() {
        final URI uri = UriComponentsBuilder.fromUriString(API_V1_MOVIES_INFO_PATH)
            .queryParam("year", 2005)
            .buildAndExpand()
            .toUri();

        webTestClient.get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(MovieInfo.class)
            .hasSize(1);
    }

    @Test
    void show() {
        final var movieInfoId = "abc";

        webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");

    }

    @Test
    void update() {
        final var movieInfoId = "abc";
        final MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises 1",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.put()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isOk()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                final MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                assertThat(updatedMovieInfo)
                    .isNotNull()
                    .matches(it -> Objects.nonNull(it.getMovieInfoId()))
                    .hasFieldOrPropertyWithValue("name", "Dark Knight Rises 1");
            });
    }

    @Test
    void delete() {
        final var movieInfoId = "abc";

        webTestClient.delete()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void updateNotFound() {
        final var movieInfoId = "def";
        final MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises 1",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        webTestClient.put()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void showNotFound() {
        final var movieInfoId = "def";

        webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .exchange()
            .expectStatus().isNotFound();
    }
}