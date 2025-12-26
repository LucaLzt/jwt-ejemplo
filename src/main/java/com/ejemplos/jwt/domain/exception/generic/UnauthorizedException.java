package com.ejemplos.jwt.domain.exception.generic;

public class UnauthorizedException extends DomainException {
    protected UnauthorizedException(String message) {
        super(message);
    }
}
