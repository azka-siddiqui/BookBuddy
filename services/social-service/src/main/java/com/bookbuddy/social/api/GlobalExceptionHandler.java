package com.bookbuddy.social.api;

import com.bookbuddy.social.auth.InvalidCredentialsException;
import com.bookbuddy.social.auth.UsernameTakenException;
import com.bookbuddy.social.club.ClubFullException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.NoSuchElementException;

/**
 * Maps auth and domain exceptions to RFC 7807 problem responses.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Authentication failed", ex.getMessage());
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ProblemDetail handleUsernameTaken(UsernameTakenException ex) {
        return problem(HttpStatus.CONFLICT, "Username unavailable", ex.getMessage());
    }

    @ExceptionHandler(ClubFullException.class)
    public ProblemDetail handleClubFull(ClubFullException ex) {
        return problem(HttpStatus.CONFLICT, "Book club is full", ex.getMessage());
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail handleNotFound(NoSuchElementException ex) {
        return problem(HttpStatus.NOT_FOUND, "Resource not found", ex.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now().toString());
        return problem;
    }
}
