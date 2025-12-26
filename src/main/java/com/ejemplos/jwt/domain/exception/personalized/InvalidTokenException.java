package com.ejemplos.jwt.domain.exception.personalized;

import com.ejemplos.jwt.domain.exception.generic.BadRequestException;

public class InvalidTokenException extends BadRequestException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
