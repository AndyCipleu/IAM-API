package com.andy.iamapi.domain.exception;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String identifier) {
        super("User not found: " + identifier);
    }
}
