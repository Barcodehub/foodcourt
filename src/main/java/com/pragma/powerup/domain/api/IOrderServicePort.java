package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IOrderServicePort {
    OrderModel createOrder(OrderModel orderModel);
}
