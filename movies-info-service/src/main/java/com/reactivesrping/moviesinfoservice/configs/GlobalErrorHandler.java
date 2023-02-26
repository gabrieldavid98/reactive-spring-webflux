package com.reactivesrping.moviesinfoservice.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> handleError(WebExchangeBindException ex) {
        log.error("Exception catch ", ex);

        final String errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(it -> it.getField() + " " + it.getDefaultMessage())
            .sorted()
            .collect(Collectors.joining(","));

        log.error("Errors are: {}", errors);

        return ResponseEntity.badRequest().body(errors);
    }
}
