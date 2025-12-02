package com.pragma.powerup.infrastructure.exceptionhandler;

public enum ExceptionResponse {
    NO_DATA_FOUND("No se encontraron datos para la petición solicitada"),
    USER_UNDERAGE("El usuario debe ser mayor de 18 años para registrarse"),
    INVALID_USER_DATA("Los datos del usuario son inválidos"),
    USER_ALREADY_EXISTS("El usuario ya existe en el sistema"),
    EMAIL_ALREADY_EXISTS("Ya existe un usuario registrado con ese email"),
    IDENTIFICATION_ALREADY_EXISTS("Ya existe un usuario registrado con ese documento de identidad"),
    DUPLICATE_USER_DATA("El email o documento de identidad ya están registrados"),
    VALIDATION_FAILED("Errores de validación en los campos proporcionados"),
    INTERNAL_SERVER_ERROR("Ha ocurrido un error inesperado. Por favor, contacte al administrador"),

    // Order related messages
    ORDER_NOT_FOUND("La orden solicitada no fue encontrada"),
    ORDER_ALREADY_ACTIVE("El usuario ya tiene un pedido activo"),
    ORDER_INVALID_STATUS_FOR_ASSIGN("Solo los pedidos en estado 'Pendiente' pueden ser asignados a un empleado"),
    ORDER_INVALID_STATUS_FOR_DELIVERY("Solo los pedidos en estado 'Listo' pueden ser marcados como entregados"),
    ORDER_CANCELLATION_NOT_ALLOWED("Lo sentimos, tu pedido ya está en preparación y no puede cancelarse"),
    ORDER_INVALID_SECURITY_PIN("El PIN de seguridad ingresado es incorrecto"),
    ORDER_SECURITY_PIN_REQUIRED("El PIN de seguridad es requerido"),

    // Authorization messages
    UNAUTHORIZED_ORDER_OPERATION("No tiene permisos para modificar esta orden"),
    UNAUTHORIZED_CANCEL_ORDER("No tiene permisos para cancelar esta orden"),
    EMPLOYEE_NOT_FOUND("Usuario no encontrado"),
    EMPLOYEE_NO_RESTAURANT("El empleado no está asignado a ningún restaurante"),
    EMPLOYEE_WRONG_RESTAURANT("No tiene permisos para modificar órdenes de este restaurante"),

    // Restaurant messages
    RESTAURANT_NOT_FOUND("No se encontró el restaurante"),
    USER_NOT_FOUND_IN_SERVICE("No se encontró el usuario"),
    RESTAURANT_ALREADY_EXISTS("Ya existe un restaurante con el NIT proporcionado"),
    RESTAURANT_OWNER_NOT_FOUND("No se encontró el usuario propietario"),
    RESTAURANT_OWNER_INVALID_ROLE("El usuario no tiene el rol de propietario"),
    RESTAURANT_NAME_EMPTY("El nombre del restaurante no puede estar vacío"),
    RESTAURANT_NAME_NUMERIC("El nombre del restaurante no puede contener solo números"),

    // Dish related
    DISH_NOT_FOUND("No se encontró el plato solicitado"),
    DISH_PRICE_INVALID("El precio debe ser mayor a 0"),
    DISH_RESTAURANT_NOT_FOUND("No se encontró el restaurante asociado al plato"),
    DISH_UNAUTHORIZED_OWNER("Solo el propietario del restaurante puede crear o modificar platos.");

    private final String message;

    ExceptionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
