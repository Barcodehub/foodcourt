package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.apifirst.api.MetricsApi;
import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.application.handler.IMetricsHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class MetricsController implements MetricsApi {

    private final IMetricsHandler metricsHandler;

    @Override
    @RequireRole(RoleEnum.PROPIETARIO)
    public ResponseEntity<OrdersDurationMetricsResponseDto> getOrdersDurationMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        log.info("Controller: Recibiendo petición de métricas de duración para restaurante {}", restaurantId);

        OrdersDurationMetricsResponseDto response = metricsHandler.getOrdersDurationMetrics(
                restaurantId,
                startDate,
                endDate,
                page,
                size,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(response);
    }

    @Override
    @RequireRole(RoleEnum.PROPIETARIO)
    public ResponseEntity<EmployeeEfficiencyMetricsResponseDto> getEmployeeEfficiencyMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer minOrdersCompleted,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        log.info("Controller: Recibiendo petición de métricas de eficiencia para restaurante {}", restaurantId);

        EmployeeEfficiencyMetricsResponseDto response = metricsHandler.getEmployeeEfficiencyMetrics(
                restaurantId,
                startDate,
                endDate,
                minOrdersCompleted,
                page,
                size,
                sortBy,
                sortDirection
        );

        return ResponseEntity.ok(response);
    }
}

