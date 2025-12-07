package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderAuditActionType;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.OrderAuditModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exceptionhandler.ExceptionResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

        orderAuditPort.registerStatusChange(OrderAuditModel.builder()
                .orderId(orderSaved.getId())
                .restaurantId(orderSaved.getRestaurant().getId())
                .clientId(orderSaved.getClient())
                .previousStatus(null)
                .newStatus(OrderStatusEnum.PENDIENT)
                .changedByUserId(orderSaved.getClient())
                .changedByRole(role)
                .actionType(OrderAuditActionType.ORDER_CREATED.getValue())
                .employeeId(null)
                .notes("Pedido creado exitosamente")
                .build());

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

        orderAuditPort.registerStatusChange(OrderAuditModel.builder()
                .orderId(updatedOrder.getId())
                .restaurantId(updatedOrder.getRestaurant().getId())
                .clientId(updatedOrder.getClient())
                .previousStatus(previousStatus)
                .newStatus(OrderStatusEnum.IN_PREPARE)
                .changedByUserId(employeeId)
                .changedByRole(role)
                .actionType(OrderAuditActionType.ASSIGNMENT.getValue())
                .employeeId(employeeId)
                .notes("Pedido asignado a empleado para preparación")
                .build());

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

        orderAuditPort.registerStatusChange(OrderAuditModel.builder()
                .orderId(updatedOrder.getId())
                .restaurantId(updatedOrder.getRestaurant().getId())
                .clientId(updatedOrder.getClient())
                .previousStatus(previousStatus)
                .newStatus(OrderStatusEnum.READY)
                .changedByUserId(employeeId)
                .changedByRole(role)
                .actionType(OrderAuditActionType.READY_FOR_PICKUP.getValue())
                .employeeId(employeeId)
                .notes("Pedido marcado como listo para recoger")
                .build());

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
        orderAuditPort.registerStatusChange(OrderAuditModel.builder()
                .orderId(updatedOrder.getId())
                .restaurantId(updatedOrder.getRestaurant().getId())
                .clientId(updatedOrder.getClient())
                .previousStatus(previousStatus)
                .newStatus(OrderStatusEnum.DELIVERED)
                .changedByUserId(employeeId)
                .changedByRole(role)
                .actionType(OrderAuditActionType.DELIVERED.getValue())
                .employeeId(employeeId)
                .notes("Pedido entregado al cliente con PIN verificado")
                .build());

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

        orderAuditPort.registerStatusChange(OrderAuditModel.builder()
                .orderId(updatedOrder.getId())
                .restaurantId(updatedOrder.getRestaurant().getId())
                .clientId(updatedOrder.getClient())
                .previousStatus(previousStatus)
                .newStatus(OrderStatusEnum.CANCELLED)
                .changedByUserId(currentUserId)
                .changedByRole(role)
                .actionType(OrderAuditActionType.CANCELLATION.getValue())
                .employeeId(null)
                .notes("Pedido cancelado por el cliente")
                .build());

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

}

