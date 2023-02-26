package com.reactivesrping.moviesinfoservice.controllers;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import com.reactivesrping.moviesinfoservice.services.MoviesInfoService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@AutoConfigureWebTestClient
@WebFluxTest(MoviesInfoController.class)
class MoviesInfoControllerTest {
    private static final String API_V1_MOVIES_INFO_PATH = "/api/v1/movies-info";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MoviesInfoService moviesInfoService;

    private List<MovieInfo> movieInfos;

    @BeforeEach
    void setUp() {
        movieInfos = List.of(
            new MovieInfo(null, "Dark Knight Rises 1",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
            new MovieInfo(null, "The Dark Knight",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))
        );
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void index() {
        when(moviesInfoService.findAll()).thenReturn(Flux.fromIterable(movieInfos));

        webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(MovieInfo.class)
            .hasSize(3);
    }

    @Test
    void show() {
        when(moviesInfoService.findById(eq("abc"))).thenReturn(Flux.fromIterable(movieInfos).last());

        final var movieInfoId = "abc";

        webTestClient.get()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo("Dark Knight Rises");
    }

    @Test
    void create() {
        final MovieInfo movieInfo = new MovieInfo(null, "Batman Begins",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.create(isA(MovieInfo.class))).thenReturn(Mono.just(movieInfo.withMovieInfoId("mockId")));

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
                    .matches(it -> Objects.nonNull(it.getMovieInfoId()))
                    .hasFieldOrPropertyWithValue("movieInfoId", "mockId");
            });
    }

    @Test
    void createValidation() {
        final MovieInfo movieInfo = new MovieInfo(null, "",
            -1, List.of(""), LocalDate.parse("2005-06-15"));

        webTestClient.post()
            .uri(API_V1_MOVIES_INFO_PATH)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .consumeWith(stringEntityExchangeResult -> {
                final String responseBody = stringEntityExchangeResult.getResponseBody();
                assertThat(responseBody)
                    .isNotNull()
                    .isEqualTo("cast[0] no debe estar vacío,name no debe estar vacío,year debe ser mayor que 0");
            });
    }

    @Test
    void update() {
        final var movieInfoId = "abc";
        final MovieInfo movieInfo = new MovieInfo(null, "Dark Knight Rises 1",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        when(moviesInfoService.update(isA(MovieInfo.class), eq("abc"))).thenReturn(Mono.just(movieInfo));

        webTestClient.put()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .bodyValue(movieInfo)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(MovieInfo.class)
            .consumeWith(movieInfoEntityExchangeResult -> {
                final MovieInfo updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();

                assertThat(updatedMovieInfo)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("name", "Dark Knight Rises 1");
            });
    }

    @Test
    void delete() {
        final var movieInfoId = "abc";

        when(moviesInfoService.delete(eq("abc"))).thenReturn(Mono.empty());

        webTestClient.delete()
            .uri(API_V1_MOVIES_INFO_PATH + "/{id}", movieInfoId)
            .exchange()
            .expectStatus().isNoContent();
    }
}