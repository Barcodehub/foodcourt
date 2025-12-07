package com.pragma.powerup.domain.exception;

public class UnauthorizedDishOperationException extends RuntimeException {
    public UnauthorizedDishOperationException(String message) {
        super(message);
    }
}

