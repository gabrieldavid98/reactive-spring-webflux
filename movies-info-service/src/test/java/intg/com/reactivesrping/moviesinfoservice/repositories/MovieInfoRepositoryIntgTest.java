package com.reactivesrping.moviesinfoservice.repositories;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
class MovieInfoRepositoryIntgTest {
    @Autowired
    private MovieInfoRepository movieInfoRepository;

    @BeforeEach
    void setUp() {
        var movieinfos = List.of(
            new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
            new MovieInfo(null, "The Dark Knight",
                2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
            new MovieInfo("abc", "Dark Knight Rises",
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        movieInfoRepository.saveAll(movieinfos).blockLast();
    }

    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block();
    }

    @Test
    void findAll() {
        final Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(movieInfoFlux)
            .expectNextCount(3)
            .verifyComplete();
    }

    @Test
    void findById() {
        final Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(movieInfoMono)
            .assertNext(movieInfo ->
                assertThat(movieInfo)
                    .isNotNull()
                    .isExactlyInstanceOf(MovieInfo.class)
                    .hasFieldOrPropertyWithValue("name", "Dark Knight Rises")
            )
            .verifyComplete();
    }

    @Test
    void saveMovieInfo() {
        final MovieInfo movieInfoToSave = new MovieInfo(null, "Batman Begins1",
            2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        final Mono<MovieInfo> movieInfoMono = movieInfoRepository.save(movieInfoToSave).log();

        StepVerifier.create(movieInfoMono)
            .assertNext(movieInfo ->
                assertThat(movieInfo)
                    .isNotNull()
                    .isExactlyInstanceOf(MovieInfo.class)
                    .hasFieldOrPropertyWithValue("name", "Batman Begins1")
                    .matches(m -> Objects.nonNull(m.getMovieInfoId()))
            )
            .verifyComplete();
    }

    @Test
    void updateMovieInfo() {
        final Mono<MovieInfo> movieInfoMono = movieInfoRepository.findById("abc")
            .map(movieInfo -> movieInfo.withYear(2021))
            .flatMap(movieInfoRepository::save)
            .log();

        StepVerifier.create(movieInfoMono)
            .assertNext(movieInfo ->
                assertThat(movieInfo)
                    .isNotNull()
                    .isExactlyInstanceOf(MovieInfo.class)
                    .hasFieldOrPropertyWithValue("year", 2021)
            )
            .verifyComplete();
    }

    @Test
    void deleteMovieInfo() {
        final Flux<MovieInfo> movieInfoFlux = movieInfoRepository.deleteById("abc")
            .thenMany(movieInfoRepository.findAll())
            .log();

        StepVerifier.create(movieInfoFlux)
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void findByYear() {
        final Flux<MovieInfo> movieInfoFlux = movieInfoRepository.findByYear(2005).log();

        StepVerifier.create(movieInfoFlux)
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void findByName() {
        final Mono<MovieInfo> movieInfoMono = movieInfoRepository.findByName("Dark Knight Rises").log();

        StepVerifier.create(movieInfoMono)
            .assertNext(movieInfo ->
                assertThat(movieInfo).hasFieldOrPropertyWithValue("name", "Dark Knight Rises")
            )
            .verifyComplete();
    }
}