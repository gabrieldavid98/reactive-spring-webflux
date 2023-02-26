package com.reactivespring.config;

import com.reactivespring.exception.MoviesInfoClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(MoviesInfoClientException.class)
    public ResponseEntity<String> handle(MoviesInfoClientException ex) {
        log.error("Exception caught: {}", ex.getMessage());

        return ResponseEntity.status(ex.getStatusCode()).body(ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handle(RuntimeException ex) {
        log.error("Exception caught: {}", ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }
}
