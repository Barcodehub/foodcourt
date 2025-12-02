package com.pragma.powerup.application.handler;

import com.pragma.powerup.apifirst.model.*;

public interface IOrderHandler {
    OrderDataResponseDto createOrder(OrderRequestDto OrderRequestDto);

    OrderListResponseDto listOrdersByStatusAndRestaurant(String status, Integer page, Integer size);
}
