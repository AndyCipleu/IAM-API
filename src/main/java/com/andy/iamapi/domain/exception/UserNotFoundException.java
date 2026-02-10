package com.andy.iamapi.domain.exception;

import java.util.UUID;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(UUID identifier) {
        super("User not found: " + identifier);
    }
}
