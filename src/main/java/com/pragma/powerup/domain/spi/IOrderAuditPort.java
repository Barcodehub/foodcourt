package com.pragma.powerup.domain.spi;

import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.domain.enums.OrderStatusEnum;

import java.util.List;

// Puerto de salida para el servicio de auditoría
public interface IOrderAuditPort {

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

    OrderStatusAuditListResponseDto getAuditHistory(
            Long clientId,
            Long orderId,
            List<String> actionTypes,
            Integer page,
            Integer size
    );
}

