package com.glpalma.HomeTreasury.web;

import com.glpalma.HomeTreasury.treasury.AccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail validation(MethodArgumentNotValidException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation failed");
        detail.setProperty("errors", ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + " " + err.getDefaultMessage())
                .toList());
        return detail;
    }

    @ExceptionHandler(ResponseStatusException.class)
    ProblemDetail statusException(ResponseStatusException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.resolve(ex.getStatusCode().value()), ex.getReason());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    ProblemDetail unknownAccount(AccountNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(RestClientException.class)
    ProblemDetail pluggyDown(RestClientException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, "Bank data provider unavailable");
    }
}