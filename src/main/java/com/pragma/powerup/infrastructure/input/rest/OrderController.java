package com.pragma.powerup.infrastructure.input.rest;


import com.pragma.powerup.apifirst.api.OrdersApi;
import com.pragma.powerup.apifirst.model.DeliverOrderRequestDto;
import com.pragma.powerup.apifirst.model.OrderDataResponseDto;
import com.pragma.powerup.apifirst.model.OrderListResponseDto;
import com.pragma.powerup.apifirst.model.OrderRequestDto;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrdersApi {

    private final IOrderHandler orderHandler;

    @Override
    @RequireRole(RoleEnum.CLIENTE)
    public ResponseEntity<OrderDataResponseDto> createOrder(OrderRequestDto orderRequestDto) {
        OrderDataResponseDto responseDto = orderHandler.createOrder(orderRequestDto);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @RequireRole(RoleEnum.EMPLEADO)
    public ResponseEntity<OrderListResponseDto> listOrdersByStatusAndRestaurant(String status, Integer page, Integer size) {
        OrderListResponseDto responseDto = orderHandler.listOrdersByStatusAndRestaurant(status, page, size);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @RequireRole(RoleEnum.EMPLEADO)
    public ResponseEntity<OrderDataResponseDto> assignOrderToEmployee(Long orderId) {
        OrderDataResponseDto responseDto = orderHandler.assignOrderToEmployee(orderId);
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @RequireRole(RoleEnum.EMPLEADO)
    public ResponseEntity<OrderDataResponseDto> deliverOrder(Long orderId, DeliverOrderRequestDto deliverOrderRequestDto) {
        OrderDataResponseDto responseDto = orderHandler.deliverOrder(orderId, deliverOrderRequestDto.getSecurityPin());
        return ResponseEntity.ok(responseDto);
    }

    @Override
    @RequireRole(RoleEnum.CLIENTE)
    public ResponseEntity<OrderDataResponseDto> cancelOrder(Long orderId) {
        OrderDataResponseDto responseDto = orderHandler.cancelOrder(orderId);
        return ResponseEntity.ok(responseDto);
    }
}
