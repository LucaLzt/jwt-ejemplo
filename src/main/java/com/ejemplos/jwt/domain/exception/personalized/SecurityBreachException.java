package com.ejemplos.jwt.domain.exception.personalized;

import com.ejemplos.jwt.domain.exception.generic.UnauthorizedException;

public class SecurityBreachException extends UnauthorizedException {
    public SecurityBreachException(String message) {
        super(message);
    }
}
