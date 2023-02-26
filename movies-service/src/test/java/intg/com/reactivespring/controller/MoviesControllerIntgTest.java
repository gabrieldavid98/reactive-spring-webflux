package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.spec.internal.MediaTypes;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.MimeType;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = {
    "restClient.moviesInfoUrl=http://localhost:8084/api/v1/movies-info",
    "restClient.reviewsUrl=http://localhost:8084/v1/reviews"
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class MoviesControllerIntgTest {
    @Autowired
    WebTestClient webTestClient;

    @Test
    void retrieveMovieById() {
        final var movieId = "abc";

        stubFor(
            get(urlEqualTo("/api/v1/movies-info/" + movieId))
                .willReturn(
                    aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                        .withBodyFile("movieinfo.json")
                )
        );

        stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                    aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                        .withBodyFile("reviews.json")
                )
        );

        webTestClient.get()
            .uri("/v1/movies/{id}", movieId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Movie.class)
            .consumeWith(movieEntityExchangeResult -> {
                final Movie movie = movieEntityExchangeResult.getResponseBody();

                assertThat(Objects.requireNonNull(movie).getReviewList())
                    .hasSize(2);

                assertThat(movie.getMovieInfo().getName())
                    .isEqualTo("Batman Begins");
            });
    }

    @Test
    void retrieveMovieById_MovieInfo_404() {
        final var movieId = "abc";

        stubFor(get(urlEqualTo("/api/v1/movies-info/" + movieId)).willReturn(aResponse().withStatus(404)));

        stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                    aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                        .withBodyFile("reviews.json")
                )
        );

        webTestClient.get()
            .uri("/v1/movies/{id}", movieId)
            .exchange()
            .expectStatus().isNotFound()
            .expectBody(String.class)
            .isEqualTo("Movie info with id abc not found");

        verify(1, getRequestedFor(urlEqualTo("/api/v1/movies-info/" + movieId)));
    }

    @Test
    void retrieveMovieById_Reviews_404() {
        final var movieId = "abc";

        stubFor(
            get(urlEqualTo("/api/v1/movies-info/" + movieId))
                .willReturn(
                    aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                        .withBodyFile("movieinfo.json")
                )
        );

        stubFor(get(urlPathEqualTo("/v1/reviews")).willReturn(aResponse().withStatus(404)));

        webTestClient.get()
            .uri("/v1/movies/{id}", movieId)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Movie.class)
            .consumeWith(movieEntityExchangeResult -> {
                final Movie movie = movieEntityExchangeResult.getResponseBody();

                assertThat(Objects.requireNonNull(movie).getReviewList())
                    .isEmpty();

                assertThat(movie.getMovieInfo().getName())
                    .isEqualTo("Batman Begins");
            });
    }

    @Test
    void retrieveMovieById_MovieInfo_5XX() {
        final var movieId = "abc";

        stubFor(
            get(urlEqualTo("/api/v1/movies-info/" + movieId))
                .willReturn(
                    aResponse().withStatus(500)
                        .withBody("MovieInfo Service Unavailable")
                )
        );

        webTestClient.get()
            .uri("/v1/movies/{id}", movieId)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(String.class)
            .isEqualTo("Server Exception in MoviesInfoService MovieInfo Service Unavailable");

        verify(4, getRequestedFor(urlEqualTo("/api/v1/movies-info/" + movieId)));
    }

    @Test
    void retrieveMovieById_Reviews_5XX() {
        final var movieId = "abc";

        stubFor(
            get(urlEqualTo("/api/v1/movies-info/" + movieId))
                .willReturn(
                    aResponse().withHeader(HttpHeaders.CONTENT_TYPE, MediaTypes.APPLICATION_JSON)
                        .withBodyFile("movieinfo.json")
                )
        );

        stubFor(
            get(urlPathEqualTo("/v1/reviews"))
                .willReturn(
                    aResponse().withStatus(500)
                        .withBody("Review Service Not Available")));

        webTestClient.get()
            .uri("/v1/movies/{id}", movieId)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(String.class)
            .isEqualTo("Server Exception in ReviewsService Review Service Not Available");

        verify(4, getRequestedFor(urlPathMatching("/v1/reviews")));
    }
}
