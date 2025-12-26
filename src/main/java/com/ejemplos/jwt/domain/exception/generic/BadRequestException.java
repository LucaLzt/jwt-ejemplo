package com.ejemplos.jwt.domain.exception.generic;

public class BadRequestException extends DomainException {
    protected BadRequestException(String message) {
        super(message);
    }
}
