package com.pragma.powerup.domain.spi;

import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.domain.enums.OrderStatusEnum;

import java.util.List;

/**
 * Puerto de salida para integración con el servicio de auditoría
 * Responsabilidad: Registrar y consultar cambios de estado de pedidos en el sistema de trazabilidad
 * Sigue el principio de inversión de dependencias (SOLID)
 */
public interface IOrderAuditPort {

    /**
     * Registra un cambio de estado de pedido en el servicio de auditoría
     */
    void registerStatusChange(
            Long orderId,
            Long restaurantId,
            Long clientId,
            OrderStatusEnum previousStatus,
            OrderStatusEnum newStatus,
            Long changedByUserId,
            String changedByRole,
            String actionType,
            Long employeeId,
            String notes
    );

    /**
     * Consulta el historial de auditoría de un cliente
     */
    OrderStatusAuditListResponseDto getAuditHistory(
            Long clientId,
            Long orderId,
            List<String> actionTypes,
            Integer page,
            Integer size
    );
}

