package com.pragma.powerup.domain.exception;

public class RestaurantAlreadyExistsException extends DomainException {
    public RestaurantAlreadyExistsException(String message) {
        super(message);
    }
}
