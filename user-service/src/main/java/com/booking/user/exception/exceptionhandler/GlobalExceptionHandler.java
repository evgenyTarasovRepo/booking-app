package com.booking.user.exception.exceptionhandler;

import com.booking.user.exception.UserAlreadyExistsException;
import com.booking.user.exception.UserNotFoundException;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex, WebRequest request) {
        log.warn("User already exists: {}", ex.getMessage());

        return createProblemDetail(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail pd = buildValidationProblemDetail((ServletWebRequest) request, errors);

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(DataIntegrityViolationException ex, WebRequest request) {
        return createProblemDetail(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.warn("Constraint violation: {}: ", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String field = violation.getPropertyPath().toString();
            String cleanField = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
            errors.put(cleanField, violation.getMessage());
        });

        return buildValidationProblemDetail((ServletWebRequest) request, errors);
    }

    private static ProblemDetail buildValidationProblemDetail(ServletWebRequest request, Map<String, String> errors) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Validation Error");
        pd.setDetail("Invalid request parameters");
        pd.setProperty("invalid_params", errors);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(URI.create(request.getRequest().getRequestURI()));
        return pd;
    }

    private static ProblemDetail createProblemDetail(String message, HttpStatus status, WebRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
        return pd;
    }
}
