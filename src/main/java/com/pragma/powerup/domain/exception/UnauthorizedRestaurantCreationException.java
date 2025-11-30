package com.pragma.powerup.domain.exception;

public class UnauthorizedRestaurantCreationException extends RuntimeException {
    public UnauthorizedRestaurantCreationException(String message) {
        super(message);
    }
}
