package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.SmsNotificationModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
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

    private static final String DIGITS = "0123456789";
    private static final int PIN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final IOrderPersistencePort orderPersistencePort;
    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ISecurityContextPort securityContextPort;
    private final IUserValidationPort userValidationPort;
    private final ISmsNotificationPort smsNotificationPort;


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
        UserResponseModel user = getUserById(userId);

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
        OrderModel order = findOrderById(orderId);
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
    public OrderModel markOrderAsReady(Long orderId) {
        OrderModel order = findOrderById(orderId);
        validateEmployeeBelongsToRestaurant(order);

        // Validar que el estado actual sea IN_PREPARE
        if (order.getStatus() != OrderStatusEnum.IN_PREPARE) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_INVALID_STATUS_FOR_READY.getMessage());
        }

        // Obtener información del cliente para enviar SMS
        UserResponseModel client = getClientWithValidPhone(order.getClient());

        // Cambiar estado a READY
        order.setStatus(OrderStatusEnum.READY);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        // Enviar SMS al cliente con el PIN de seguridad
        sendOrderReadyNotification(client, updatedOrder);

        return updatedOrder;
    }

    @Override
    @Transactional
    public OrderModel deliverOrder(Long orderId, String securityPin) {
        // Validar que el PIN no sea nulo o vacío
        if (securityPin == null || securityPin.trim().isEmpty()) {
            throw new InvalidSecurityPinException(ExceptionResponse.ORDER_SECURITY_PIN_REQUIRED.getMessage());
        }

        OrderModel order = findOrderById(orderId);
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
        OrderModel order = findOrderById(orderId);

        // Validar que el cliente que cancela sea el dueño de la orden
        Long currentUserId = securityContextPort.getCurrentUserId();
        if (!order.getClient().equals(currentUserId)) {
            throw new UnauthorizedOperationException(ExceptionResponse.UNAUTHORIZED_CANCEL_ORDER.getMessage());
        }

        // Validar que el estado actual sea PENDIENT
        if (order.getStatus() != OrderStatusEnum.PENDIENT) {
            throw new OrderCancellationException(ExceptionResponse.ORDER_CANCELLATION_NOT_ALLOWED.getMessage());
        }

        // Obtener información del cliente para enviar SMS
        UserResponseModel client = getClientWithValidPhone(order.getClient());

        // Actualizar el estado a CANCELLED
        order.setStatus(OrderStatusEnum.CANCELLED);
        OrderModel updatedOrder = orderPersistencePort.updateOrder(order);

        // Enviar notificación de cancelación al cliente
        sendOrderCancelledNotification(client, updatedOrder);

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

    /**
     * Obtiene una orden por su ID
     * @param orderId ID de la orden
     * @return OrderModel encontrado
     * @throws OrderNotFoundException si la orden no existe
     */
    private OrderModel findOrderById(Long orderId) {
        return orderPersistencePort.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.ORDER_NOT_FOUND.getMessage()));
    }

    /**
     * Obtiene un usuario por su ID
     * @param userId ID del usuario
     * @return UserResponseModel encontrado
     * @throws OrderNotFoundException si el usuario no existe
     */
    private UserResponseModel getUserById(Long userId) {
        return userValidationPort.getUserById(userId)
                .orElseThrow(() -> new OrderNotFoundException(ExceptionResponse.USER_NOT_FOUND_IN_SERVICE.getMessage()));
    }

    /**
     * Obtiene un cliente y valida que tenga número de teléfono
     * @param clientId ID del cliente
     * @return UserResponseModel con teléfono válido
     * @throws InvalidOrderStatusException si el cliente no tiene teléfono
     */
    private UserResponseModel getClientWithValidPhone(Long clientId) {
        UserResponseModel client = getUserById(clientId);

        if (client.getPhoneNumber() == null || client.getPhoneNumber().trim().isEmpty()) {
            throw new InvalidOrderStatusException(ExceptionResponse.ORDER_CLIENT_PHONE_NOT_FOUND.getMessage());
        }

        return client;
    }

    /**
     * Envía notificación SMS genérica al cliente
     * @param client Información del cliente
     * @param message Mensaje a enviar
     * @param order Orden relacionada
     */
    private void sendSmsNotification(UserResponseModel client, String message, OrderModel order) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", order.getId().toString());
        metadata.put("restaurantName", order.getRestaurant().getName());

        SmsNotificationModel smsNotification = new SmsNotificationModel(
                client.getPhoneNumber(),
                message,
                metadata
        );

        smsNotificationPort.sendSms(smsNotification);
    }

    /**
     * Envía notificación cuando el pedido está listo
     */
    private void sendOrderReadyNotification(UserResponseModel client, OrderModel order) {
        String message = String.format(
                "Hola %s, tu pedido está listo para ser recogido en %s. Tu PIN de seguridad es: %s",
                client.getName(),
                order.getRestaurant().getName(),
                order.getSecurityPin()
        );

        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", order.getId().toString());
        metadata.put("restaurantName", order.getRestaurant().getName());
        metadata.put("securityPin", order.getSecurityPin());

        SmsNotificationModel smsNotification = new SmsNotificationModel(
                client.getPhoneNumber(),
                message,
                metadata
        );

        smsNotificationPort.sendSms(smsNotification);
    }

    /**
     * Envía notificación cuando el pedido es cancelado
     */
    private void sendOrderCancelledNotification(UserResponseModel client, OrderModel order) {
        String message = String.format(
                "Hola %s, tu pedido en %s ha sido cancelado exitosamente.",
                client.getName(),
                order.getRestaurant().getName()
        );

        sendSmsNotification(client, message, order);
    }

    private String generateSecurityPin() {
        StringBuilder pin = new StringBuilder(PIN_LENGTH);
        for (int i = 0; i < PIN_LENGTH; i++) {
            pin.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return pin.toString();
    }

}

