package com.reactivespring.controllers;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {
    private final MoviesInfoRestClient moviesInfoRestClient;
    private final ReviewsRestClient reviewsRestClient;

    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")
    public Mono<Movie> retrieveMovieById(@PathVariable("id") String movieId) {
        return moviesInfoRestClient.retrieveMovieInfo(movieId)
            .flatMap(movieInfo -> {
                final Mono<List<Review>> reviewsMono = reviewsRestClient.retrieveReviews(movieId).collectList();
                return reviewsMono.map(reviews -> new Movie(movieInfo, reviews));
            });
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> retrieveMovieInfos() {
        return moviesInfoRestClient.retrieveMovieInfoStream();
    }
}
