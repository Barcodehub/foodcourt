package com.pragma.powerup.domain.model;


import com.pragma.powerup.domain.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderModel {

    private Long id;
    private RestaurantModel restaurant;
    private List<OrderDishModel> dishes;
    private OrderStatusEnum status;
    private Long client;
    private Long employee;
    private LocalDateTime createdAt;

}



