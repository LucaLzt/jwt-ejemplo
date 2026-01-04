package com.ejemplos.jwt.infrastructure.web.exception;

import com.ejemplos.jwt.domain.exception.generic.BadRequestException;
import com.ejemplos.jwt.domain.exception.generic.ConflictException;
import com.ejemplos.jwt.domain.exception.generic.ResourceNotFound;
import com.ejemplos.jwt.domain.exception.generic.UnauthorizedException;
import com.ejemplos.jwt.domain.exception.personalized.SecurityBreachException;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Interceptor global de excepciones.
 * <p>
 * Transforma las excepciones de Dominio (Java) en respuestas HTTP JSON estandarizadas (RFC 7807 ProblemDetail).
 * Esto evita que el cliente reciba stack traces feos o códigos 500 genéricos.
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_URI_BASE = "https://api.midominio.com/errors/";

    @ExceptionHandler(BadRequestException.class)
    public ProblemDetail handleBadRequest(BadRequestException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
        problem.setTitle("Bad Request");
        problem.setType(URI.create(ERROR_URI_BASE + "bad-request"));
        return problem;
    }

    @ExceptionHandler(ResourceNotFound.class)
    public ProblemDetail handleResourceNotFound(ResourceNotFound e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Resource Not Found");
        problem.setType(URI.create(ERROR_URI_BASE + "resource-not-found"));
        return problem;
    }

    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Resource Conflict");
        problem.setType(URI.create(ERROR_URI_BASE + "resource-conflict"));
        return problem;
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorized(UnauthorizedException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
        problem.setTitle("Unauthorized");
        problem.setType(URI.create(ERROR_URI_BASE + "unauthorized"));
        return problem;
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleDomainException(BadCredentialsException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        problem.setTitle("Unauthorized");
        problem.setType(URI.create(ERROR_URI_BASE + "unauthorized"));
        return problem;
    }

    @ExceptionHandler(SecurityBreachException.class)
    public ProblemDetail handleSecurityBreach(SecurityBreachException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
        problem.setTitle("Security Breach Detected");
        problem.setType(URI.create(ERROR_URI_BASE + "security-breach"));
        return problem;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Validation Failed");
        problem.setTitle("Validation Error");
        problem.setType(URI.create(ERROR_URI_BASE + "validation-error"));

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        problem.setProperty("errors", errors);
        return ResponseEntity.status(status).body(problem);
    }
}
