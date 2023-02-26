package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Slf4j
@Component
public class ReviewHandler {
    private final Sinks.Many<Review> reviewsSink = Sinks.many().replay().all();

    private final ReviewReactiveRepository reviewReactiveRepository;

    private final Validator validator;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository, Validator validator) {
        this.reviewReactiveRepository = reviewReactiveRepository;
        this.validator = validator;
    }

    public Mono<ServerResponse> create(ServerRequest request) {
        return request.bodyToMono(Review.class)
            .doOnNext(this::validate)
            .flatMap(reviewReactiveRepository::save)
            .doOnNext(reviewsSink::tryEmitNext)
            .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    private void validate(Review review) {
        final Set<ConstraintViolation<Review>> constraintViolations = validator.validate(review);
        log.info("ConstraintViolations : {}", constraintViolations);

        if (constraintViolations.isEmpty()) {
            return;
        }

        final String errorMessages = constraintViolations.stream()
            .map(ConstraintViolation::getMessage)
            .sorted()
            .collect(joining(","));

        throw new ReviewDataException(errorMessages);
    }

    public Mono<ServerResponse> index(ServerRequest request) {
        final Optional<String> movieInfoIdOptional = request.queryParam("movieInfoId");

        if (movieInfoIdOptional.isPresent()) {
            final Flux<Review> reviewFlux =
                reviewReactiveRepository.findByMovieInfoId(Long.parseLong(movieInfoIdOptional.get()));
            return buildReviewsResponse(reviewFlux);
        }

        final Flux<Review> reviewFlux = reviewReactiveRepository.findAll();
        return buildReviewsResponse(reviewFlux);
    }

    private Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviewFlux) {
        return ServerResponse.ok().body(reviewFlux, Review.class);
    }

    public Mono<ServerResponse> update(ServerRequest request) {
        final String reviewId = request.pathVariable("id");

        final Mono<Review> existingReviewMono = reviewReactiveRepository.findById(reviewId)
            .switchIfEmpty(
                Mono.error(new ReviewNotFoundException("Review not found fot the given review id " + reviewId))
            );

        return existingReviewMono.flatMap(review ->
            request.bodyToMono(Review.class)
                .map(requestReview ->
                    review.withComment(requestReview.getComment())
                        .withRating(requestReview.getRating())
                )
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.ok()::bodyValue)
        );
    }

    public Mono<ServerResponse> delete(ServerRequest request) {
        final String reviewId = request.pathVariable("id");

        final Mono<Review> existingReviewMono = reviewReactiveRepository.findById(reviewId);

        return existingReviewMono.flatMap(review ->
            reviewReactiveRepository.deleteById(review.getReviewId())
                .then(ServerResponse.noContent().build())
        );
    }

    public Mono<ServerResponse> getReviewsStream(ServerRequest serverRequest) {
        return ServerResponse.ok()
            .contentType(MediaType.APPLICATION_NDJSON)
            .body(reviewsSink.asFlux(), Review.class)
            .log();
    }
}
