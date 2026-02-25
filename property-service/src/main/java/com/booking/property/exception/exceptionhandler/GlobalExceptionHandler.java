package com.booking.property.exception.exceptionhandler;

import com.booking.property.exception.OwnerNotFoundException;
import com.booking.property.exception.PropertyNotFoundException;
import com.booking.property.exception.UserServiceUnavailableException;
import feign.FeignException;
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

    @ExceptionHandler(PropertyNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ProblemDetail handlePropertyNotFound(PropertyNotFoundException ex, WebRequest request) {
        log.warn("Property not found {}", ex.getMessage());

        return createProblemDetail(ex.getMessage(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler(OwnerNotFoundException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleOwnerNotFound(OwnerNotFoundException ex, WebRequest request) {
        log.warn("Owner not found {}", ex.getMessage());

        return createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public ProblemDetail handleUserServiceUnavailable(UserServiceUnavailableException ex, WebRequest request) {
        log.warn("User service unavailable {}", ex.getMessage());

        return createProblemDetail(ex.getMessage(), HttpStatus.SERVICE_UNAVAILABLE, request);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );

        ProblemDetail pd = createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        pd.setTitle("Validation Error");
        pd.setDetail("Invalid request parameters");
        pd.setProperty("invalid_params", errors);

        return ResponseEntity.badRequest().body(pd);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, WebRequest request) {
        log.warn("Validation error: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            var fieldName = extractFieldName(violation.getPropertyPath().toString());
            var message = violation.getMessage();
            errors.put(fieldName, message);
        });

        ProblemDetail pd = createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        pd.setTitle("Validation Error");
        pd.setDetail("Invalid request parameters");
        pd.setProperty("invalid_params", errors);

        return pd;
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ProblemDetail handleDuplicateEmail(DataIntegrityViolationException ex, WebRequest request) {
        return createProblemDetail(ex.getMessage(), HttpStatus.CONFLICT, request);
    }

    @ExceptionHandler(FeignException.class)
    public ProblemDetail handleFeignException(FeignException ex, WebRequest request) {
        log.warn("Feign error: status={}, message={}", ex.status(), ex.getMessage());

        HttpStatus status = switch (ex.status()) {
            case 404 -> HttpStatus.NOT_FOUND;
            case 400 -> HttpStatus.BAD_REQUEST;
            case 503, 502, 504 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };

        String message = switch (ex.status()) {
            case 400 -> "Bad request";
            case 404 -> "Referenced resource not found";
            case 503, 502, 504 -> "Dependent service is temporarily unavailable";
            default -> "Error communicating with dependent service";
        };

        return createProblemDetail(message, status, request);
    }

    private static ProblemDetail createProblemDetail(String message, HttpStatus status, WebRequest request) {
        var pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(URI.create(((ServletWebRequest) request).getRequest().getRequestURI()));
        return pd;
    }

    private String extractFieldName(String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        return parts[parts.length - 1];
    }
}
