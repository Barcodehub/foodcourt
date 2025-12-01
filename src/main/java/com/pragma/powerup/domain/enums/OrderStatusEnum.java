package com.pragma.powerup.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    PENDIENT("Pendiente"),
    IN_PREPARE("En Preparación"),
    READY("Listo"),
    DELIVERED("Entregado"),
    CANCELLED("Cancelado");

    private final String displayName;

    public static OrderStatusEnum fromString(String statusName) {
        for (OrderStatusEnum status : OrderStatusEnum.values()) {
            if (status.name().equalsIgnoreCase(statusName) ||
                status.displayName.equalsIgnoreCase(statusName)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de orden no válido: " + statusName);
    }
}
