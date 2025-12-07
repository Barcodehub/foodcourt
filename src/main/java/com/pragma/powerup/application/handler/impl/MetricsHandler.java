package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.application.handler.IMetricsHandler;
import com.pragma.powerup.domain.api.IMetricsServicePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetricsHandler implements IMetricsHandler {

    private final IMetricsServicePort metricsServicePort;

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
        log.info("Handler: Consultando métricas de duración de pedidos para restaurante {}", restaurantId);

        return metricsServicePort.getOrdersDurationMetrics(
                restaurantId,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection
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
        log.info("Handler: Consultando métricas de eficiencia de empleados para restaurante {}", restaurantId);

        return metricsServicePort.getEmployeeEfficiencyMetrics(
                restaurantId,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection
        );
    }
}

