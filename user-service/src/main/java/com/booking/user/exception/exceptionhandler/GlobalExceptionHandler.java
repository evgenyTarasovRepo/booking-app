package com.booking.user.exception.exceptionhandler;

import com.booking.user.exception.UserNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        log.warn("User not found: {}", ex.getMessage());

        return createProblemDetail(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("MethodArgumentNotValidException: {}", ex.getMessage());
        var pd = ex.getBody();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getGlobalErrors().forEach(e -> {
            errors.put(e.getObjectName(), e.getDefaultMessage());
        });
        ex.getBindingResult().getFieldErrors().forEach(e -> {
            errors.put(e.getField(), e.getDefaultMessage());
        });
        log.error("Intercepted MethodArgumentNotValidException. Errors: {}", errors);
        pd.setProperty("invalid_params", errors);
        pd.setStatus(HttpStatus.BAD_REQUEST);
        pd.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
        return handleExceptionInternal(ex, pd, headers, status, request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(DataIntegrityViolationException ex, WebRequest request) {
        return createProblemDetail(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    private static ProblemDetail createProblemDetail(String message, HttpStatus status, WebRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
        return pd;
    }
}
