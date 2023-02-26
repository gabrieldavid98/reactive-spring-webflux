package com.reactivesrping.moviesinfoservice.controllers;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.stream.IntStream;

public class SinksTest {
    @Test
    void sinks() {
        final Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        IntStream.rangeClosed(1, 100).parallel()
            .forEach(replaySink::tryEmitNext);

        final Flux<Integer> integerFlux = replaySink.asFlux();
        integerFlux.subscribe(System.out::println);
    }

    @Test
    void sinksMulticast() {
        final Sinks.Many<Integer> multicastSink = Sinks.many().multicast().onBackpressureBuffer();

        multicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        multicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        final Flux<Integer> integerFlux = multicastSink.asFlux();
        integerFlux.subscribe(System.out::println);
    }

    @Test
    void sinksUnicast() {
        final Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();

        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST);
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        final Flux<Integer> integerFlux = unicastSink.asFlux();
        integerFlux.subscribe(System.out::println);
    }
}
