package com.pragma.powerup.domain.exception;

/**
 * Excepción lanzada cuando un propietario intenta crear/modificar un plato de un restaurante que no le pertenece
 */
public class UnauthorizedDishOperationException extends RuntimeException {
    public UnauthorizedDishOperationException(String message) {
        super(message);
    }
}

