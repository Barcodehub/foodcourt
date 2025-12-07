package com.pragma.powerup.infrastructure.out.http.adapter;

import com.pragma.powerup.apifirst.model.OrderStatusAuditListResponseDto;
import com.pragma.powerup.apifirst.model.OrderStatusAuditRequestDto;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import com.pragma.powerup.infrastructure.out.http.client.IOrderAuditClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Adaptador HTTP para el servicio de auditoría
 * Responsabilidad: Implementar el puerto de auditoría usando Feign Client
 * Sigue el patrón de arquitectura hexagonal (Adapter)
 * Aplica el principio de inversión de dependencias (SOLID)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAuditHttpAdapter implements IOrderAuditPort {

    private final IOrderAuditClient orderAuditClient;

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
}


