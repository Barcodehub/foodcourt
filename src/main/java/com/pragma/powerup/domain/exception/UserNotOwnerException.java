package com.pragma.powerup.domain.exception;

public class UserNotOwnerException extends DomainException {
    public UserNotOwnerException(String message) {
        super(message);
    }
}
