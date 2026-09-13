package com.bookbuddy.discovery.api;

import com.bookbuddy.discovery.client.OpenLibraryException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

/**
 * Translates service-level exceptions into RFC 7807 problem responses so the
 * gateway and frontend receive consistent, informative error bodies.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OpenLibraryException.class)
    public ProblemDetail handleOpenLibrary(OpenLibraryException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_GATEWAY, ex.getMessage());
        problem.setTitle("Open Library source unavailable");
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
