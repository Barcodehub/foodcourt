package com.pragma.powerup.domain.exception;

public class InvalidOrderStatusException extends DomainException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }
}

