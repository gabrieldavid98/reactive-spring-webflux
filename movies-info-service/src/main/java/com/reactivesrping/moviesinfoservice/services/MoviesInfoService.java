package com.reactivesrping.moviesinfoservice.services;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import com.reactivesrping.moviesinfoservice.repositories.MovieInfoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {
    private final MovieInfoRepository movieInfoRepository;

    public MoviesInfoService(MovieInfoRepository movieInfoRepository) {
        this.movieInfoRepository = movieInfoRepository;
    }

    public Mono<MovieInfo> create(MovieInfo movieInfo) {
        return movieInfoRepository.save(movieInfo);
    }

    public Flux<MovieInfo> findAll() {
        return movieInfoRepository.findAll();
    }

    public Mono<MovieInfo> findById(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo> update(MovieInfo movieInfoToUpdate, String id) {
        return movieInfoRepository.findById(id)
            .map(movieInfo ->
                movieInfo
                    .withCast(movieInfoToUpdate.getCast())
                    .withName(movieInfoToUpdate.getName())
                    .withReleaseDate(movieInfoToUpdate.getReleaseDate())
                    .withYear(movieInfoToUpdate.getYear())
            )
            .flatMap(movieInfoRepository::save);
    }

    public Mono<Void> delete(String id) {
        return movieInfoRepository.deleteById(id);
    }

    public Flux<MovieInfo> findByYear(int year) {
        return movieInfoRepository.findByYear(year);
    }
}
