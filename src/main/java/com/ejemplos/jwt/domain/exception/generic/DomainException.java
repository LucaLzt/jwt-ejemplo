package com.ejemplos.jwt.domain.exception.generic;

public class DomainException extends RuntimeException {
    protected DomainException(String message) {
        super(message);
    }
}
