package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UnauthorizedDishOperationException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class OrderUseCase implements IOrderServicePort {

    private final IOrderPersistencePort orderPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ISecurityContextPort securityContextPort;


    @Override
    @Transactional
    public OrderModel createOrder(OrderModel orderModel) {
        validOneOrderByUser(orderModel);
        orderModel.setStatus(OrderStatusEnum.PENDIENT);
        OrderModel orderSaved = orderPersistencePort.saveOrder(orderModel);
        return orderSaved;
    }

    private void validOneOrderByUser(OrderModel orderModel) {
        Long userId = securityContextPort.getCurrentUserId();
        orderModel.setClient(userId);
        Optional<OrderModel> existingOrder = orderPersistencePort.getActiveOrderByUserId(userId);
        if (existingOrder.isPresent()) {
            throw new UnauthorizedDishOperationException("El usuario ya tiene un pedido activo");
        }
    }

}
