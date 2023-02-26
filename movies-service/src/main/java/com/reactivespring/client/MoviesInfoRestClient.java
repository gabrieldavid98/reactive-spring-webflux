package com.reactivespring.client;

import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;

@Slf4j
@Component
public class MoviesInfoRestClient {
    private final WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")
    private String moviesInfoUrl;

    public MoviesInfoRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {
        final String url = moviesInfoUrl.concat("/{id}");

        return webClient.get()
            .uri(url, movieId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                log.info("Status code is : {}", clientResponse.statusCode().value());

                if (clientResponse.statusCode() == HttpStatus.NOT_FOUND) {
                    return Mono.error(new MoviesInfoClientException(
                        String.format("Movie info with id %s not found", movieId),
                        clientResponse.statusCode().value()
                    ));
                }

                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                        responseMessage, clientResponse.statusCode().value()
                    )));
            })
            .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                log.info("Status code is : {}", clientResponse.statusCode().value());

                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                        "Server Exception in MoviesInfoService " + responseMessage
                    )));
            })
            .bodyToMono(MovieInfo.class)
            .retryWhen(RetryUtil.retrySpec())
            .log();
    }

    public Flux<MovieInfo> retrieveMovieInfoStream() {
        final String url = moviesInfoUrl.concat("/stream");

        return webClient.get()
            .uri(url)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError, clientResponse -> {
                log.info("Status code is : {}", clientResponse.statusCode().value());

                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> Mono.error(new MoviesInfoClientException(
                        responseMessage, clientResponse.statusCode().value()
                    )));
            })
            .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                log.info("Status code is : {}", clientResponse.statusCode().value());

                return clientResponse.bodyToMono(String.class)
                    .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                        "Server Exception in MoviesInfoService " + responseMessage
                    )));
            })
            .bodyToFlux(MovieInfo.class)
            .retryWhen(RetryUtil.retrySpec())
            .log();
    }
}
