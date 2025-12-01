package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.*;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderHandler implements IOrderHandler {

    private final IOrderMapper orderMapper;
    private final IOrderServicePort orderServicePort;


    @Override
    public OrderDataResponseDto createOrder(OrderRequestDto OrderRequestDto) {
        OrderModel orderModel = orderMapper.toModel(OrderRequestDto);
        OrderModel savedOrder = orderServicePort.createOrder(orderModel);
        OrderResponseDto responseDto = orderMapper.toResponseDto(savedOrder);

        OrderDataResponseDto dataResponse = new OrderDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Orden creada exitosamente");
        return dataResponse;
    }
}
