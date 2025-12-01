package com.pragma.powerup.domain.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderDishModel {

    private Long id;
    private OrderModel order;
    private DishModel dish;
    private Integer quantity;

}