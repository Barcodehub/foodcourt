package com.pragma.powerup.domain.spi;

import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.domain.model.OrderAuditModel;

import java.time.OffsetDateTime;
import java.util.List;

// Puerto de salida para el servicio de auditoría
public interface IOrderAuditPort {

    void registerStatusChange(OrderAuditModel auditModel);

    OrderStatusAuditListResponseDto getAuditHistory(
            Long clientId,
            Long orderId,
            List<String> actionTypes,
            Integer page,
            Integer size
    );

    OrdersDurationMetricsResponseDto getOrdersDurationMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    );

    EmployeeEfficiencyMetricsResponseDto getEmployeeEfficiencyMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    );
}

