package com.reactivesrping.moviesinfoservice.controllers;

import com.reactivesrping.moviesinfoservice.domain.MovieInfo;
import com.reactivesrping.moviesinfoservice.services.MoviesInfoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/movies-info")
public class MoviesInfoController {
    private final MoviesInfoService moviesInfoService;

    private final Sinks.Many<MovieInfo> moviesInfoSink;

    public MoviesInfoController(MoviesInfoService moviesInfoService) {
        this.moviesInfoService = moviesInfoService;
        moviesInfoSink = Sinks.many().replay().all();
    }

    @GetMapping
    public Flux<MovieInfo> index(@RequestParam(required = false) Integer year) {
        if (year != null) {
            return moviesInfoService.findByYear(year);
        }

        return moviesInfoService.findAll().log();
    }

    @GetMapping("{id}")
    public Mono<ResponseEntity<MovieInfo>> show(@PathVariable String id) {
        return moviesInfoService.findById(id)
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> stream() {
        return moviesInfoSink.asFlux();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<MovieInfo> create(@RequestBody @Valid MovieInfo movieInfo) {
        return moviesInfoService.create(movieInfo)
            .doOnNext(moviesInfoSink::tryEmitNext)
            .log();
    }

    @PutMapping("{id}")
    public Mono<ResponseEntity<MovieInfo>> update(@RequestBody MovieInfo movieInfoToSave, @PathVariable String id) {
        return moviesInfoService.update(movieInfoToSave, id)
            .map(ResponseEntity::ok)
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));
    }

    @DeleteMapping("{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> delete(@PathVariable String id) {
        return moviesInfoService.delete(id);
    }
}
