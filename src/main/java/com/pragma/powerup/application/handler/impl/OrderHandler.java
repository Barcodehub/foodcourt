package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.*;
import com.pragma.powerup.application.handler.IOrderHandler;
import com.pragma.powerup.application.mapper.IOrderMapper;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.model.OrderModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Override
    public OrderListResponseDto listOrdersByStatusAndRestaurant(String status, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(
                page != null ? page : 0,
                size != null ? size : 10
        );

        Page<OrderModel> orderPage = orderServicePort.listOrdersByStatusAndRestaurant(status, pageable);
        OrderListResponseDto responseDto = orderMapper.toListResponseDto(orderPage);
        return responseDto;
    }

    @Override
    public OrderDataResponseDto assignOrderToEmployee(Long orderId) {
        OrderModel assignedOrder = orderServicePort.assignOrderToEmployee(orderId);
        OrderResponseDto responseDto = orderMapper.toResponseDto(assignedOrder);

        OrderDataResponseDto dataResponse = new OrderDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Orden asignada exitosamente y estado cambiado a 'En Preparación'");
        return dataResponse;
    }

    @Override
    public OrderDataResponseDto deliverOrder(Long orderId, String securityPin) {
        OrderModel deliveredOrder = orderServicePort.deliverOrder(orderId, securityPin);
        OrderResponseDto responseDto = orderMapper.toResponseDto(deliveredOrder);

        OrderDataResponseDto dataResponse = new OrderDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Orden marcada como entregada exitosamente");
        return dataResponse;
    }

    @Override
    public OrderDataResponseDto cancelOrder(Long orderId) {
        OrderModel cancelledOrder = orderServicePort.cancelOrder(orderId);
        OrderResponseDto responseDto = orderMapper.toResponseDto(cancelledOrder);

        OrderDataResponseDto dataResponse = new OrderDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Orden cancelada exitosamente");
        return dataResponse;
    }
}
