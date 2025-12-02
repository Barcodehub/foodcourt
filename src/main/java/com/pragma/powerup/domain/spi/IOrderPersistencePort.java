package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IOrderPersistencePort {
    OrderModel saveOrder(OrderModel dish);

    Optional<OrderModel> getActiveOrderByUserId(Long userId);

    Page<OrderModel> listOrdersByStatusAndRestaurant(OrderStatusEnum status, Long restaurantId, Pageable pageable);

    Page<OrderModel> listOrdersByRestaurant(Long restaurantId, Pageable pageable);
}

