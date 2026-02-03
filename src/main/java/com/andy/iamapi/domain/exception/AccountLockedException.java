package com.andy.iamapi.domain.exception;

public class AccountLockedException extends RuntimeException {
    public AccountLockedException(String email) {
        super("Account " + email + " is locked");
    }
}
