package com.pragma.powerup.infrastructure.out.http.client;

import com.pragma.powerup.apifirst.model.OrderStatusAuditDataResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditRequestDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Cliente Feign para comunicación con el microservicio de trazabilidad-auditoría
 * Responsabilidad: Definir el contrato de comunicación HTTP
 * Usa Feign para abstracción de llamadas HTTP
 */
@FeignClient(
        name = "traceability-audit-service",
        url = "${audit.service.url:http://localhost:8083}"
)
public interface IOrderAuditClient {

    /**
     * Registra una auditoría de cambio de estado en el servicio de trazabilidad
     */
    @PostMapping("/audit/order-status")
    OrderStatusAuditDataResponseDto registerAudit(@RequestBody OrderStatusAuditRequestDto request);

    /**
     * Consulta el historial de auditoría con filtros
     */
    @GetMapping("/audit/order-status")
    OrderStatusAuditListResponseDto getAuditHistory(
            @RequestParam(value = "clientId", required = false) Long clientId,
            @RequestParam(value = "orderId", required = false) Long orderId,
            @RequestParam(value = "actionTypes", required = false) List<String> actionTypes,
            @RequestParam(value = "page", defaultValue = "0") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size
    );
}

