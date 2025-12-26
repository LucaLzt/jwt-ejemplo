package com.ejemplos.jwt.domain.exception.generic;

public class ConflictException extends DomainException {
    protected ConflictException(String message) {
        super(message);
    }
}
