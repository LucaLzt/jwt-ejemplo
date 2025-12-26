package com.ejemplos.jwt.domain.exception.personalized;

import com.ejemplos.jwt.domain.exception.generic.ConflictException;

public class EmailAlreadyExistsException extends ConflictException {
    public EmailAlreadyExistsException(String email) {
        super("The email address is already in use: " + email);
    }
}
