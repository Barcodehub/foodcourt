package com.pragma.powerup.domain.enums;

public enum OrderAuditActionType {
    ORDER_CREATED("ORDER_CREATED"),
    ASSIGNMENT("ASSIGNMENT"),
    READY_FOR_PICKUP("READY_FOR_PICKUP"),
    DELIVERED("DELIVERED"),
    CANCELLATION("CANCELLATION");

    private final String value;

    OrderAuditActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}

