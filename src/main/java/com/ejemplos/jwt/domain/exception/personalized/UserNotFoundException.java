package com.ejemplos.jwt.domain.exception.personalized;

import com.ejemplos.jwt.domain.exception.generic.ResourceNotFound;

public class UserNotFoundException extends ResourceNotFound {
    public UserNotFoundException(String message) {
        super(message);
    }
}
