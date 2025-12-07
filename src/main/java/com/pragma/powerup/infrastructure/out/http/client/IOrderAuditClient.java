package com.pragma.powerup.infrastructure.out.http.client;

import com.pragma.powerup.apifirst.model.OrderStatusAuditDataResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditRequestDto;
import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.OffsetDateTime;
import java.util.List;

@FeignClient(
        name = "traceability-audit-service",
        url = "${audit.service.url:http://localhost:8083}"
)
public interface IOrderAuditClient {

    @PostMapping("/audit/order-status")
    OrderStatusAuditDataResponseDto registerAudit(@RequestBody OrderStatusAuditRequestDto request);

    @GetMapping("/audit/order-status")
    OrderStatusAuditListResponseDto getAuditHistory(
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "actionTypes", required = false) List<String> actionTypes,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );

    @GetMapping("/audit/metrics/orders-duration")
    OrdersDurationMetricsResponseDto getOrdersDurationMetrics(
            @RequestParam(value = "restaurantId") Long restaurantId,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "durationMinutes") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "DESC") String sortDirection
    );

    @GetMapping("/audit/metrics/employee-efficiency")
    EmployeeEfficiencyMetricsResponseDto getEmployeeEfficiencyMetrics(
            @RequestParam(value = "restaurantId") Long restaurantId,
            @RequestParam(value = "startDate", required = false) OffsetDateTime startDate,
            @RequestParam(value = "endDate", required = false) OffsetDateTime endDate,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "20") Integer size,
            @RequestParam(value = "sortBy", defaultValue = "averageDurationMinutes") String sortBy,
            @RequestParam(value = "sortDirection", defaultValue = "ASC") String sortDirection
    );
}

