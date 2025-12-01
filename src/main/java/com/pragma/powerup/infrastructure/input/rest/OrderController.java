package com.pragma.powerup.infrastructure.input.rest;


import com.pragma.powerup.apifirst.api.OrdersApi;
import com.pragma.powerup.apifirst.model.OrderDataResponseDto;
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
}
