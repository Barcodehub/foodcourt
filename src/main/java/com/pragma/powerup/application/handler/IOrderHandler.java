package com.pragma.powerup.application.handler;

import com.pragma.powerup.apifirst.model.*;

import java.util.List;

public interface IOrderHandler {
    OrderDataResponseDto createOrder(OrderRequestDto OrderRequestDto);

    OrderListResponseDto listOrdersByStatusAndRestaurant(String status, Integer page, Integer size);

    OrderDataResponseDto assignOrderToEmployee(Long orderId);

    OrderDataResponseDto markOrderAsReady(Long orderId);

    OrderDataResponseDto deliverOrder(Long orderId, String securityPin);

    OrderDataResponseDto cancelOrder(Long orderId);

    /**
     * Consulta el historial de auditoría de los pedidos del cliente autenticado
     */
    OrderStatusAuditListResponseDto getMyOrdersAuditHistory(Long orderId, List<String> actionTypes, Integer page, Integer size);
}
