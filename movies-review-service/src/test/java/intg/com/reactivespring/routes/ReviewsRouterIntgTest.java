package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.iterable;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@ActiveProfiles("test")
@AutoConfigureWebTestClient
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class ReviewsRouterIntgTest {
    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    private static final String REVIEWS_URL = "/v1/reviews";

    @BeforeEach
    void setUp() {
        final List<Review> reviews = List.of(
            new Review("abc", 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0));

        reviewReactiveRepository.saveAll(reviews).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void create() {
        final var review = new Review(null, 3L, "Awesome Movie", 9.0);

        webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(review)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(Review.class)
            .consumeWith(reviewEntityExchangeResult -> {
                final Review savedReview = reviewEntityExchangeResult.getResponseBody();
                assertThat(savedReview)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("movieInfoId", 3L)
                    .matches(it -> Objects.nonNull(it.getReviewId()));
            });
    }

    @Test
    void getAllReviews() {
        webTestClient
            .get()
            .uri(REVIEWS_URL)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Review.class)
            .hasSize(3);
    }

    @Test
    void update() {
        final var reviewId = "abc";
        final var reviewToUpdate = new Review(null, 1L, "Cool Movie", 9.3);

        webTestClient
            .put()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .bodyValue(reviewToUpdate)
            .exchange()
            .expectStatus().isOk()
            .expectBody(Review.class)
            .consumeWith(reviewEntityExchangeResult -> {
                final Review updatedReview = reviewEntityExchangeResult.getResponseBody();
                assertThat(updatedReview)
                    .isNotNull()
                    .hasFieldOrPropertyWithValue("comment", "Cool Movie")
                    .hasFieldOrPropertyWithValue("rating", 9.3);
            });
    }

    @Test
    void delete() {
        final var reviewId = "abc";

        webTestClient
            .delete()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .exchange()
            .expectStatus().isNoContent();

        webTestClient
            .get()
            .uri(REVIEWS_URL)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Review.class)
            .hasSize(2);
    }

    @Test
    void getAllReviewsByMovieInfoId() {
        final URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 1L)
            .buildAndExpand()
            .toUri();

        webTestClient
            .get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Review.class)
            .hasSize(2);
    }
}
