package com.pragma.powerup.infrastructure.exception;

import org.springframework.http.HttpStatus;

public class RemoteServiceException extends RuntimeException {
    private final HttpStatus status;

    public RemoteServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

