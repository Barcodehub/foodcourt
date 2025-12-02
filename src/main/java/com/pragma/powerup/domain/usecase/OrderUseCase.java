package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UnauthorizedDishOperationException;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
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
    private final IUserValidationPort userValidationPort;


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

    @Override
    public Page<OrderModel> listOrdersByStatusAndRestaurant(String status, Pageable pageable) {
        Long userId = securityContextPort.getCurrentUserId();

        // Obtener información del empleado desde el microservicio de usuarios
        Optional<UserResponseModel> userOptional = userValidationPort.getUserById(userId);
        if (userOptional.isEmpty()) {
            throw new RestaurantNotFoundException("No se encontró el usuario");
        }

        UserResponseModel user = userOptional.get();
        if (user.getRestaurantWorkId() == null) {
            throw new RestaurantNotFoundException("El empleado no está asignado a ningún restaurante");
        }

        Long restaurantId = user.getRestaurantWorkId();

        // Si se especifica status, filtrar por status, sino traer todos
        if (status != null && !status.trim().isEmpty()) {
            OrderStatusEnum statusEnum = OrderStatusEnum.fromString(status);
            return orderPersistencePort.listOrdersByStatusAndRestaurant(statusEnum, restaurantId, pageable);
        } else {
            return orderPersistencePort.listOrdersByRestaurant(restaurantId, pageable);
        }
    }

}

