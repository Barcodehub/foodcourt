package com.pragma.powerup.infrastructure.out.http.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditRequestDto;
import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import com.pragma.powerup.infrastructure.exception.RemoteServiceException;
import com.pragma.powerup.infrastructure.out.http.client.IOrderAuditClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuditHttpAdapter implements IOrderAuditPort {

    private final IOrderAuditClient orderAuditClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void registerStatusChange(
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
    ) {
        try {
            OrderStatusAuditRequestDto request = new OrderStatusAuditRequestDto();
            request.setOrderId(orderId);
            request.setRestaurantId(restaurantId);
            request.setClientId(clientId);
            request.setPreviousStatus(previousStatus != null ? previousStatus.name() : null);
            request.setNewStatus(newStatus.name());
            request.setChangedByUserId(changedByUserId);
            request.setChangedByRole(changedByRole);
            request.setActionType(actionType);
            request.setEmployeeId(employeeId);
            request.setNotes(notes);

            log.info("Registrando auditoría para orden {} - Estado: {} -> {}",
                    orderId,
                    previousStatus != null ? previousStatus.name() : "NUEVA",
                    newStatus.name());

            orderAuditClient.registerAudit(request);

            log.info("Auditoría registrada exitosamente para orden {}", orderId);

        } catch (Exception e) {
            // no lanzamos excepción para no interrumpir el flujo del negocio
            log.error("Error al registrar auditoría para orden {}: {}",
                    orderId, e.getMessage(), e);
        }
    }

    @Override
    public OrderStatusAuditListResponseDto getAuditHistory(
            Long clientId,
            Long orderId,
            List<String> actionTypes,
            Integer page,
            Integer size
    ) {
        try {
            log.info("Consultando historial de auditoría - Cliente: {}, Pedido: {}", clientId, orderId);

            OrderStatusAuditListResponseDto response = orderAuditClient.getAuditHistory(
                    clientId,
                    orderId,
                    actionTypes,
                    page,
                    size
            );

            log.info("Historial obtenido - {} registros",
                    response.getData() != null ? response.getData().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Error al consultar historial de auditoría: {}", e.getMessage(), e);
            //  respuesta vacía
            return new OrderStatusAuditListResponseDto();
        }
    }

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
        try {
            log.info("Consultando métricas de duración de pedidos - Restaurante: {}, Rango: {} - {}",
                    restaurantId, startDate, endDate);

            OrdersDurationMetricsResponseDto response = orderAuditClient.getOrdersDurationMetrics(
                    restaurantId,
                    startDate,
                    endDate,
                    page,
                    size,
                    sortBy,
                    sortDirection
            );

            log.info("Métricas de duración obtenidas exitosamente");

            return response;

        } catch (FeignException e) {
            handleFeignException(e, "getOrdersDurationMetrics");
            return null; 
        }
    }

    @Override
    public EmployeeEfficiencyMetricsResponseDto getEmployeeEfficiencyMetrics(
            Long restaurantId,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Integer minOrdersCompleted,
            Integer page,
            Integer size,
            String sortBy,
            String sortDirection
    ) {
        try {
            log.info("Consultando métricas de eficiencia de empleados - Restaurante: {}, Rango: {} - {}",
                    restaurantId, startDate, endDate);

            EmployeeEfficiencyMetricsResponseDto response = orderAuditClient.getEmployeeEfficiencyMetrics(
                    restaurantId,
                    startDate,
                    endDate,
                    minOrdersCompleted,
                    page,
                    size,
                    sortBy,
                    sortDirection
            );

            log.info("Métricas de eficiencia obtenidas exitosamente");

            return response;

        } catch (FeignException e) {
            handleFeignException(e, "getOrdersDurationMetrics");
            return null;
        }
    }



    private void handleFeignException(FeignException e, String methodName) {
        String errorMessage = "Error al comunicarse con el servicio de auditoría";
        try {
            String responseBody = e.contentUTF8();
            if (responseBody != null && !responseBody.isEmpty()) {
                Map<String, Object> errorMap = objectMapper.readValue(responseBody, Map.class);
                if (errorMap.containsKey("message")) {
                    errorMessage = errorMap.get("message").toString();
                } else if (errorMap.containsKey("error")) {
                    errorMessage = errorMap.get("error").toString();
                }
            }
        } catch (IOException ex) {
            log.error("Error al parsear respuesta de error: {}", ex.getMessage());
        }

        HttpStatus status = HttpStatus.resolve(e.status());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        log.error("Error del servicio de auditoría [{}]: Status {}, Mensaje: {}",
                methodName, status.value(), errorMessage);

        throw new RemoteServiceException(errorMessage, status);
    }
}
