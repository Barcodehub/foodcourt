package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.domain.api.IMetricsServicePort;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;

@Slf4j
@RequiredArgsConstructor
public class MetricsUseCase implements IMetricsServicePort {

    private final IOrderAuditPort orderAuditPort;

    @Override
    public OrdersDurationMetricsResponseDto getOrdersDurationMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        log.info("Obteniendo métricas de duración de pedidos para restaurante: {}", restaurantId);

        return orderAuditPort.getOrdersDurationMetrics(
                restaurantId,
                startDate,
                endDate,
                page != null ? page : 0,
                size != null ? size : 20,
                sortBy != null ? sortBy : "durationMinutes",
                sortDirection != null ? sortDirection : "DESC"
        );
    }

    @Override
    public EmployeeEfficiencyMetricsResponseDto getEmployeeEfficiencyMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        log.info("Obteniendo métricas de eficiencia de empleados para restaurante: {}", restaurantId);

        return orderAuditPort.getEmployeeEfficiencyMetrics(
                restaurantId,
                startDate,
                endDate,
                page != null ? page : 0,
                size != null ? size : 20,
                sortBy != null ? sortBy : "averageDurationMinutes",
                sortDirection != null ? sortDirection : "ASC"
        );
    }
}

