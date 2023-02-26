package com.reactivesrping.moviesinfoservice.repositories;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo, String> {
    Flux<MovieInfo> findByYear(int year);
    Mono<MovieInfo> findByName(String name);
}
