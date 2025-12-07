package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderAuditActionType;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.SmsNotificationModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.domain.spi.ISmsNotificationPort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exceptionhandler.ExceptionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
public class OrderUseCase implements IOrderServicePort {

    private final IOrderPersistencePort orderPersistencePort;
    private final ISecurityContextPort securityContextPort;
    private final IUserValidationPort userValidationPort;
    private final IOrderAuditPort orderAuditPort;
    private final SmsUseCase smsUseCase;


    @Override
    @Transactional
    public OrderModel createOrder(OrderModel orderModel) {
        validOneOrderByUser(orderModel);
        orderModel.setStatus(OrderStatusEnum.PENDIENT);
        orderModel.setSecurityPin(smsUseCase.generateSecurityPin());
        OrderModel orderSaved = orderPersistencePort.saveOrder(orderModel);

        String role = getRoleOfCurrentUser();

        registerAudit(
                orderSaved.getId(),
                orderSaved.getRestaurant().getId(),
                orderSaved.getClient(),
                null,
                OrderStatusEnum.PENDIENT,
                orderSaved.getClient(),
                role,
                OrderAuditActionType.ORDER_CREATED.getValue(),
                null,
                "Pedido creado exitosamente"
        );

        return orderSaved;
    }

    private String getRoleOfCurrentUser() {
         Long userId = securityContextPort.getCurrentUserId();
        UserResponseModel user = getUserById(userId);
        return user.getRole();
    }

    @Override
    public Page<OrderModel> listOrdersByStatusAndRestaurant(String status, Pageable pageable) {
        Long userId = securityContextPort.getCurrentUserId();
        UserResponseModel user = getUserById(userId);

        if (user.getRestaurantWorkId() == null) {
            throw new RestaurantNotFoundException(ExceptionResponse.EMPLOYEE_NO_RESTAURANT.getMessage());
        }

        Long restaurantId = user.getRestaurantWorkId();

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
        OrderModel order = findOrderById(orderId);
        Long employeeId = securityContextPort.getCurrentUserId();
        validateEmployeeBelongsToRestaurant(order);

        if (order.getStatus() != OrderStatusEnum.PENDIENT) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_ASSIGN.getMessage());
        }

        OrderStatusEnum previousStatus = order.getStatus();

        order.setEmployee(employeeId);
        order.setStatus(OrderStatusEnum.IN_PREPARE);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        String role = getRoleOfCurrentUser();

        registerAudit(
                updatedOrder.getId(),
                updatedOrder.getRestaurant().getId(),
                updatedOrder.getClient(),
                previousStatus,
                OrderStatusEnum.IN_PREPARE,
                employeeId,
                role,
                OrderAuditActionType.ASSIGNMENT.getValue(),
                employeeId,
                "Pedido asignado a empleado para preparación"
        );

        return updatedOrder;
    }

    @Override
    @Transactional
    public OrderModel markOrderAsReady(Long orderId) {
        OrderModel order = findOrderById(orderId);
        Long employeeId = securityContextPort.getCurrentUserId();
        validateEmployeeBelongsToRestaurant(order);

        if (order.getStatus() != OrderStatusEnum.IN_PREPARE) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_READY.getMessage());
        }

        OrderStatusEnum previousStatus = order.getStatus();

        // Obtener información del cliente para enviar SMS
        UserResponseModel client = getClientWithValidPhone(order.getClient());

        order.setStatus(OrderStatusEnum.READY);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        String role = getRoleOfCurrentUser();

        registerAudit(
                updatedOrder.getId(),
                updatedOrder.getRestaurant().getId(),
                updatedOrder.getClient(),
                previousStatus,
                OrderStatusEnum.READY,
                employeeId,
                role,
                OrderAuditActionType.READY_FOR_PICKUP.getValue(),
                employeeId,
                "Pedido marcado como listo para recoger"
        );

        // Enviar SMS al cliente con el PIN de seguridad
        smsUseCase.sendOrderReadyNotification(client, updatedOrder);

        return updatedOrder;
    }

    @Override
    @Transactional
    public OrderModel deliverOrder(Long orderId, String securityPin) {
        if (securityPin == null || securityPin.trim().isEmpty()) {
            throw new InvalidSecurityPinException(ExceptionResponse.ORDER_SECURITY_PIN_REQUIRED.getMessage());
        }

        OrderModel order = findOrderById(orderId);
        Long employeeId = securityContextPort.getCurrentUserId();
        validateEmployeeBelongsToRestaurant(order);

        //validar que yo sea el empleado asignado
        if (!order.getEmployee().equals(employeeId)) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_NOT_ASSIGNED_TO_ORDER.getMessage());
        }

        if (order.getStatus() != OrderStatusEnum.READY) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_DELIVERY.getMessage());
        }

        if (!securityPin.equals(order.getSecurityPin())) {
            throw new InvalidSecurityPinException(ExceptionResponse.ORDER_INVALID_SECURITY_PIN.getMessage());
        }

        OrderStatusEnum previousStatus = order.getStatus();

        order.setStatus(OrderStatusEnum.DELIVERED);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        String role = getRoleOfCurrentUser();

        // Registrar auditoría de entrega
        registerAudit(
                updatedOrder.getId(),
                updatedOrder.getRestaurant().getId(),
                updatedOrder.getClient(),
                previousStatus,
                OrderStatusEnum.DELIVERED,
                employeeId,
                role,
                OrderAuditActionType.DELIVERED.getValue(),
                employeeId,
                "Pedido entregado al cliente con PIN verificado"
        );

        return updatedOrder;
    }

    @Override
    @Transactional
    public OrderModel cancelOrder(Long orderId) {
        OrderModel order = findOrderById(orderId);

        // Validar que el cliente que cancela sea el dueño de la orden
        Long currentUserId = securityContextPort.getCurrentUserId();
        if (!order.getClient().equals(currentUserId)) {
            throw new UnauthorizedOperationException(ExceptionResponse.UNAUTHORIZED_CANCEL_ORDER.getMessage());
        }

        if (order.getStatus() != OrderStatusEnum.PENDIENT) {
            throw new OrderCancellationException(ExceptionResponse.ORDER_CANCELLATION_NOT_ALLOWED.getMessage());
        }

        OrderStatusEnum previousStatus = order.getStatus();

        UserResponseModel client = getClientWithValidPhone(order.getClient());

        order.setStatus(OrderStatusEnum.CANCELLED);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        String role = getRoleOfCurrentUser();

        registerAudit(
                updatedOrder.getId(),
                updatedOrder.getRestaurant().getId(),
                updatedOrder.getClient(),
                previousStatus,
                OrderStatusEnum.CANCELLED,
                currentUserId,
                role,
                OrderAuditActionType.CANCELLATION.getValue(),
                null,
                "Pedido cancelado por el cliente"
        );

        smsUseCase.sendOrderCancelledNotification(client, updatedOrder);

        return updatedOrder;
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
        UserResponseModel employee = getUserById(employeeId);

        if (employee.getRestaurantWorkId() == null) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_NO_RESTAURANT.getMessage());
        }

        // Validar que el empleado pertenezca al restaurante de la orden
        if (!employee.getRestaurantWorkId().equals(order.getRestaurant().getId())) {
            throw new UnauthorizedOperationException(ExceptionResponse.EMPLOYEE_WRONG_RESTAURANT.getMessage());
        }
    }


    private OrderModel findOrderById(Long orderId) {
        return orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.ORDER_NOT_FOUND.getMessage()));
    }


    private UserResponseModel getUserById(Long userId) {
        return userValidationPort.getUserById(userId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.USER_NOT_FOUND_IN_SERVICE.getMessage()));
    }


    private UserResponseModel getClientWithValidPhone(Long clientId) {
        UserResponseModel client = getUserById(clientId);

        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_CLIENT_PHONE_NOT_FOUND.getMessage());
        }

        return client;
    }

    private void registerAudit(
            Long orderId,
            Long restaurantId,
            Long clientId,
            OrderStatusEnum previousStatus,
            OrderStatusEnum newStatus,
            Long changedByUserId,
            String changedByRole,
            String actionType,
            Long employeeId,
            String notes
    ) {
        orderAuditPort.registerStatusChange(
                orderId, restaurantId, clientId,
                previousStatus, newStatus,
                changedByUserId, changedByRole,
                actionType, employeeId, notes
        );
    }

}

