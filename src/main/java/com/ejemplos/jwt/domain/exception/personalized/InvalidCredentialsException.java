package com.ejemplos.jwt.domain.exception.personalized;

import com.ejemplos.jwt.domain.exception.generic.UnauthorizedException;

public class InvalidCredentialsException extends UnauthorizedException {
    public InvalidCredentialsException() {
        super("Invalid email or password.");
    }
}
