package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exceptionhandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@AutoConfigureWebTestClient
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})
public class ReviewsUnitTest {
    @MockBean
    private ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    private WebTestClient webTestClient;

    private static final String REVIEWS_URL = "/v1/reviews";

    @Test
    void create() {
        final var review = new Review("abc", 3L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

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
    void create_validation() {
        final var review = new Review(null, null, "Awesome Movie", -9.0);

        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(review));

        webTestClient
            .post()
            .uri(REVIEWS_URL)
            .bodyValue(review)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(String.class)
            .isEqualTo("rating.movieInfoId : must not be null,rating.negative : please pass a non-negative value");
    }

    @Test
    void getAllReviews() {
        final List<Review> reviews = List.of(
            new Review("abc", 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0),
            new Review(null, 2L, "Excellent Movie", 8.0)
        );

        when(reviewReactiveRepository.findAll()).thenReturn(Flux.fromIterable(reviews));

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
        final var review = new Review("abc", 1L, "Awesome Movie", 9.0);
        final var reviewToUpdate = new Review(null, 1L, "Cool Movie", 9.3);

        when(reviewReactiveRepository.findById(eq("abc"))).thenReturn(Mono.just(review));
        when(reviewReactiveRepository.save(isA(Review.class))).thenReturn(Mono.just(reviewToUpdate));

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
        final var review = new Review("abc", 1L, "Awesome Movie", 9.0);

        when(reviewReactiveRepository.findById(eq("abc"))).thenReturn(Mono.just(review));
        when(reviewReactiveRepository.deleteById(eq("abc"))).thenReturn(Mono.empty());

        webTestClient
            .delete()
            .uri(REVIEWS_URL + "/{id}", reviewId)
            .exchange()
            .expectStatus().isNoContent();
    }

    @Test
    void getAllReviewsByMovieInfoId() {
        final List<Review> reviews = List.of(
            new Review("abc", 1L, "Awesome Movie", 9.0),
            new Review(null, 1L, "Awesome Movie1", 9.0)
        );

        final URI uri = UriComponentsBuilder.fromUriString(REVIEWS_URL)
            .queryParam("movieInfoId", 1L)
            .buildAndExpand()
            .toUri();

        when(reviewReactiveRepository.findByMovieInfoId(isA(Long.class))).thenReturn(Flux.fromIterable(reviews));

        webTestClient
            .get()
            .uri(uri)
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(Review.class)
            .hasSize(2);
    }
}
