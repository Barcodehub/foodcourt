package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exceptionhandler.ExceptionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.util.Optional;

@RequiredArgsConstructor
public class OrderUseCase implements IOrderServicePort {

    private static final String DIGITS = "0123456789";
    private static final int PIN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

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
        orderModel.setSecurityPin(generateSecurityPin());
        OrderModel orderSaved = orderPersistencePort.saveOrder(orderModel);
        // TODO: Enviar el PIN al cliente por SMS/Email
        return orderSaved;
    }

    @Override
    public Page<OrderModel> listOrdersByStatusAndRestaurant(String status, Pageable pageable) {
        Long userId = securityContextPort.getCurrentUserId();

        // Obtener información del empleado desde el microservicio de usuarios
        Optional<UserResponseModel> userOptional = userValidationPort.getUserById(userId);
        if (userOptional.isEmpty()) {
            throw new RestaurantNotFoundException(ExceptionResponse.USER_NOT_FOUND_IN_SERVICE.getMessage());
        }

        UserResponseModel user = userOptional.get();
        if (user.getRestaurantWorkId() == null) {
            throw new RestaurantNotFoundException(ExceptionResponse.EMPLOYEE_NO_RESTAURANT.getMessage());
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

    @Override
    @Transactional
    public OrderModel assignOrderToEmployee(Long orderId) {
        // Obtener la orden
        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.ORDER_NOT_FOUND.getMessage()));

        // Validar que el empleado pertenezca al restaurante de la orden
        Long employeeId = securityContextPort.getCurrentUserId();
        validateEmployeeBelongsToRestaurant(order);

        // Validar que el estado actual sea PENDIENT
        if (order.getStatus() != OrderStatusEnum.PENDIENT) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_ASSIGN.getMessage());
        }

        // Asignar el empleado y cambiar estado a IN_PREPARE
        order.setEmployee(employeeId);
        order.setStatus(OrderStatusEnum.IN_PREPARE);
        return orderPersistencePort.updateOrder(order);
    }

    @Override
    @Transactional
    public OrderModel deliverOrder(Long orderId, String securityPin) {
        // Validar que el PIN no sea nulo o vacío
        if (securityPin == null || securityPin.trim().isEmpty()) {
            throw new InvalidSecurityPinException(ExceptionResponse.ORDER_SECURITY_PIN_REQUIRED.getMessage());
        }

        // Obtener la orden
        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.ORDER_NOT_FOUND.getMessage()));

        // Validar que el empleado pertenezca al restaurante de la orden
        validateEmployeeBelongsToRestaurant(order);

        // Validar que el estado actual sea READY
        if (order.getStatus() != OrderStatusEnum.READY) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_DELIVERY.getMessage());
        }

        // Validar el PIN de seguridad
        if (!securityPin.equals(order.getSecurityPin())) {
            throw new InvalidSecurityPinException(ExceptionResponse.ORDER_INVALID_SECURITY_PIN.getMessage());
        }

        // Actualizar el estado a DELIVERED
        order.setStatus(OrderStatusEnum.DELIVERED);
        return orderPersistencePort.updateOrder(order);
    }

    @Override
    @Transactional
    public OrderModel cancelOrder(Long orderId) {
        // Obtener la orden
        OrderModel order = orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.ORDER_NOT_FOUND.getMessage()));

        // Validar que el cliente que cancela sea el dueño de la orden
        Long currentUserId = securityContextPort.getCurrentUserId();
        if (!order.getClient().equals(currentUserId)) {
            throw new UnauthorizedOperationException(ExceptionResponse.UNAUTHORIZED_CANCEL_ORDER.getMessage());
        }

        // Validar que el estado actual sea PENDIENT
        if (order.getStatus() != OrderStatusEnum.PENDIENT) {
            throw new OrderCancellationException(ExceptionResponse.ORDER_CANCELLATION_NOT_ALLOWED.getMessage());
        }

        // Actualizar el estado a CANCELLED
        order.setStatus(OrderStatusEnum.CANCELLED);
        return orderPersistencePort.updateOrder(order);
    }

    private void validOneOrderByUser(OrderModel orderModel) {
        Long userId = securityContextPort.getCurrentUserId();
        orderModel.setClient(userId);
        Optional<OrderModel> existingOrder = orderPersistencePort.getActiveOrderByUserId(userId);
        if (existingOrder.isPresent()) {
            throw new UnauthorizedDishOperationException(ExceptionResponse.ORDER_ALREADY_ACTIVE.getMessage());
        }
    }

    private void validateEmployeeBelongsToRestaurant(OrderModel order) {
        Long employeeId = securityContextPort.getCurrentUserId();

        // Obtener información del empleado desde el microservicio de usuarios
        Optional<UserResponseModel> userOptional = userValidationPort.getUserById(employeeId);
        if (userOptional.isEmpty()) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_NOT_FOUND.getMessage());
        }

        UserResponseModel employee = userOptional.get();
        if (employee.getRestaurantWorkId() == null) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_NO_RESTAURANT.getMessage());
        }

        // Validar que el empleado pertenezca al restaurante de la orden
        if (!employee.getRestaurantWorkId().equals(order.getRestaurant().getId())) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_WRONG_RESTAURANT.getMessage());
        }
    }

    private String generateSecurityPin() {
        StringBuilder pin = new StringBuilder(PIN_LENGTH);
        for (int i = 0; i < PIN_LENGTH; i++) {
            pin.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return pin.toString();
    }

}

