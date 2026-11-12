package com.fixflow.exception;

import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<?> notFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<?> invalid(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<?> conflict(ConflictException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
    }

    @ExceptionHandler(AuthenticationRequiredException.class)
    ResponseEntity<?> authenticationRequired(AuthenticationRequiredException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    ResponseEntity<?> forbidden(ForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("timestamp", Instant.now(), "message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<?> validation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream().findFirst().map(error -> error.getField() + " is invalid.").orElse("Request validation failed.");
        return ResponseEntity.badRequest().body(Map.of("timestamp", Instant.now(), "message", message));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<?> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(Map.of("timestamp", Instant.now(), "message", "This HTTP method is not supported for the requested endpoint."));
    }

    @ExceptionHandler(Exception.class)
ResponseEntity<?> unexpected(Exception ex) {
    logger.error("Unexpected request failure", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Map.of(
            "timestamp", Instant.now(),
            "message", "Something went wrong while processing the request."
        ));
}
}
