package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para OrderUseCase
 * Cubre las siguientes Historias de Usuario:
 * - HU11: Realizar pedido
 * - HU12: Obtener lista de pedidos filtrando por estado
 * - HU13: Asignarse a un pedido y cambiar estado a "en preparación"
 * - HU14: Notificar que el pedido está listo
 * - HU15: Entregar pedido (Marcarlo como entregado)
 * - HU16: Cancelar pedido
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderUseCase - Gestión de Pedidos")
class OrderUseCaseTest {

    @Mock
    private IOrderPersistencePort orderPersistencePort;

    @Mock
    private ISecurityContextPort securityContextPort;

    @Mock
    private IUserValidationPort userValidationPort;

    @Mock
    private IOrderAuditPort orderAuditPort;

    @Mock
    private SmsUseCase smsUseCase;

    @InjectMocks
    private OrderUseCase orderUseCase;

    private OrderModel validOrder;
    private RestaurantModel restaurant;
    private UserResponseModel client;
    private UserResponseModel employee;

    private static final Long CLIENT_ID = 1L;
    private static final Long EMPLOYEE_ID = 2L;
    private static final Long RESTAURANT_ID = 1L;
    private static final String SECURITY_PIN = "123456";

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantModel();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setName("Test Restaurant");

        client = new UserResponseModel();
        client.setId(CLIENT_ID);
        client.setName("Cliente");
        client.setRole("CLIENTE");
        client.setPhoneNumber("+573001234567");

        employee = new UserResponseModel();
        employee.setId(EMPLOYEE_ID);
        employee.setName("Empleado");
        employee.setRole("EMPLEADO");
        employee.setRestaurantWorkId(RESTAURANT_ID);

        validOrder = new OrderModel();
        validOrder.setId(1L);
        validOrder.setRestaurant(restaurant);
        validOrder.setClient(CLIENT_ID);
        validOrder.setStatus(OrderStatusEnum.PENDIENT);
        validOrder.setSecurityPin(SECURITY_PIN);
    }

    @Nested
    @DisplayName("HU11: Realizar pedido")
    class CreateOrderTests {

        @Test
        @DisplayName("Happy Path: Debe crear pedido correctamente")
        void shouldCreateOrderSuccessfully() {
            // Arrange
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.empty());
            when(smsUseCase.generateSecurityPin()).thenReturn(SECURITY_PIN);
            when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenReturn(validOrder);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            doNothing().when(orderAuditPort).registerStatusChange(any());

            // Act
            OrderModel result = orderUseCase.createOrder(validOrder);

            // Assert
            assertNotNull(result);
            assertEquals(OrderStatusEnum.PENDIENT, result.getStatus());
            assertNotNull(result.getSecurityPin());
            verify(orderPersistencePort).saveOrder(any(OrderModel.class));
            verify(orderAuditPort).registerStatusChange(any());
        }

        @Test
        @DisplayName("Validación: Debe asignar estado PENDIENT automáticamente")
        void shouldSetPendingStatusAutomatically() {
            // Arrange
            validOrder.setStatus(null);
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.empty());
            when(smsUseCase.generateSecurityPin()).thenReturn(SECURITY_PIN);
            when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenReturn(validOrder);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            doNothing().when(orderAuditPort).registerStatusChange(any());

            // Act
            orderUseCase.createOrder(validOrder);

            // Assert
            verify(orderPersistencePort).saveOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.PENDIENT
            ));
        }

        @Test
        @DisplayName("Error: Debe rechazar si el cliente ya tiene un pedido activo")
        void shouldRejectWhenClientHasActiveOrder() {
            // Arrange
            OrderModel activeOrder = new OrderModel();
            activeOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.of(activeOrder));

            // Act & Assert
            assertThrows(UnauthorizedDishOperationException.class, () -> orderUseCase.createOrder(validOrder));
            verify(orderPersistencePort, never()).saveOrder(any());
        }
    }

    @Nested
    @DisplayName("HU12: Obtener lista de pedidos filtrando por estado")
    class ListOrdersTests {

        @Test
        @DisplayName("Happy Path: Debe listar pedidos filtrados por estado")
        void shouldListOrdersFilteredByStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByStatusAndRestaurant(
                OrderStatusEnum.PENDIENT, RESTAURANT_ID, pageable
            )).thenReturn(orderPage);

            // Act
            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant("PENDIENT", pageable);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(orderPersistencePort).listOrdersByStatusAndRestaurant(OrderStatusEnum.PENDIENT, RESTAURANT_ID, pageable);
        }

        @Test
        @DisplayName("Validación: Debe listar todos los pedidos sin filtro de estado")
        void shouldListAllOrdersWhenNoStatusFilter() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByRestaurant(RESTAURANT_ID, pageable)).thenReturn(orderPage);

            // Act
            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant(null, pageable);

            // Assert
            assertNotNull(result);
            verify(orderPersistencePort).listOrdersByRestaurant(RESTAURANT_ID, pageable);
        }

        @Test
        @DisplayName("Error: Debe rechazar si el empleado no tiene restaurante asignado")
        void shouldRejectWhenEmployeeHasNoRestaurant() {
            // Arrange
            employee.setRestaurantWorkId(null);
            Pageable pageable = PageRequest.of(0, 10);

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(RestaurantNotFoundException.class,
                () -> orderUseCase.listOrdersByStatusAndRestaurant("PENDIENT", pageable));
        }
    }

    @Nested
    @DisplayName("HU13: Asignarse a un pedido y cambiar estado a 'en preparación'")
    class AssignOrderTests {

        @Test
        @DisplayName("Happy Path: Debe asignar pedido a empleado correctamente")
        void shouldAssignOrderToEmployeeSuccessfully() {
            // Arrange
            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());

            // Act
            OrderModel result = orderUseCase.assignOrderToEmployee(validOrder.getId());

            // Assert
            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getEmployee().equals(EMPLOYEE_ID) &&
                order.getStatus() == OrderStatusEnum.IN_PREPARE
            ));
            verify(orderAuditPort).registerStatusChange(any());
        }

        @Test
        @DisplayName("Validación: Debe rechazar asignación si pedido no está PENDIENT")
        void shouldRejectAssignmentWhenOrderNotPending() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.assignOrderToEmployee(validOrder.getId()));
            verify(orderPersistencePort, never()).updateOrder(any());
        }

        @Test
        @DisplayName("Error: Debe rechazar si empleado no pertenece al restaurante")
        void shouldRejectWhenEmployeeNotFromRestaurant() {
            // Arrange
            employee.setRestaurantWorkId(999L); // Otro restaurante

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.assignOrderToEmployee(validOrder.getId()));
        }
    }

    @Nested
    @DisplayName("HU14: Notificar que el pedido está listo")
    class MarkOrderAsReadyTests {

        @Test
        @DisplayName("Happy Path: Debe marcar pedido como listo y enviar SMS")
        void shouldMarkOrderAsReadyAndSendSms() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());
            doNothing().when(smsUseCase).sendOrderReadyNotification(any(), any());

            // Act
            OrderModel result = orderUseCase.markOrderAsReady(validOrder.getId());

            // Assert
            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.READY
            ));
            verify(smsUseCase).sendOrderReadyNotification(any(UserResponseModel.class), any(OrderModel.class));
        }

        @Test
        @DisplayName("Validación: Debe rechazar si pedido no está EN PREPARACIÓN")
        void shouldRejectWhenOrderNotInPrepare() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.PENDIENT);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.markOrderAsReady(validOrder.getId()));
        }

        @Test
        @DisplayName("Error: Debe rechazar si cliente no tiene teléfono")
        void shouldRejectWhenClientHasNoPhone() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            client.setPhoneNumber(null);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));

            // Act & Assert
            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.markOrderAsReady(validOrder.getId()));
        }
    }

    @Nested
    @DisplayName("HU15: Entregar pedido")
    class DeliverOrderTests {

        @Test
        @DisplayName("Happy Path: Debe entregar pedido con PIN correcto")
        void shouldDeliverOrderWithCorrectPin() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());

            // Act
            OrderModel result = orderUseCase.deliverOrder(validOrder.getId(), SECURITY_PIN);

            // Assert
            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.DELIVERED
            ));
        }

        @Test
        @DisplayName("Validación: Debe rechazar PIN incorrecto")
        void shouldRejectIncorrectPin() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(InvalidSecurityPinException.class,
                () -> orderUseCase.deliverOrder(validOrder.getId(), "wrongPIN"));
        }

        @Test
        @DisplayName("Error: Debe rechazar si pedido no está LISTO")
        void shouldRejectWhenOrderNotReady() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.deliverOrder(validOrder.getId(), SECURITY_PIN));
        }

        @Test
        @DisplayName("Validación: Debe rechazar si empleado no es el asignado")
        void shouldRejectWhenEmployeeNotAssigned() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(999L); // Otro empleado

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            // Act & Assert
            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.deliverOrder(validOrder.getId(), SECURITY_PIN));
        }

        @Test
        @DisplayName("Error: Debe rechazar PIN vacío")
        void shouldRejectEmptyPin() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.READY);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));

            // Act & Assert
            assertThrows(InvalidSecurityPinException.class,
                () -> orderUseCase.deliverOrder(validOrder.getId(), ""));
        }
    }

    @Nested
    @DisplayName("HU16: Cancelar pedido")
    class CancelOrderTests {

        @Test
        @DisplayName("Happy Path: Debe cancelar pedido PENDIENT correctamente")
        void shouldCancelPendingOrderSuccessfully() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.PENDIENT);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());
            doNothing().when(smsUseCase).sendOrderCancelledNotification(any(), any());

            // Act
            OrderModel result = orderUseCase.cancelOrder(validOrder.getId());

            // Assert
            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.CANCELLED
            ));
            verify(smsUseCase).sendOrderCancelledNotification(any(), any());
        }

        @Test
        @DisplayName("Validación: Debe rechazar cancelación si no es el cliente dueño")
        void shouldRejectCancellationByNonOwner() {
            // Arrange
            validOrder.setClient(999L); // Otro cliente

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);

            // Act & Assert
            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.cancelOrder(validOrder.getId()));
        }

        @Test
        @DisplayName("Error: Debe rechazar cancelación si pedido no está PENDIENT")
        void shouldRejectCancellationWhenOrderNotPending() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);

            // Act & Assert
            assertThrows(OrderCancellationException.class,
                () -> orderUseCase.cancelOrder(validOrder.getId()));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Pedido inexistente debe lanzar excepción")
        void shouldThrowExceptionForNonExistentOrder() {
            // Arrange
            when(orderPersistencePort.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(OrderNotFoundException.class,
                () -> orderUseCase.assignOrderToEmployee(999L));
        }

        @Test
        @DisplayName("Edge Case: PIN null debe ser rechazado")
        void shouldRejectNullPin() {
            // Arrange
            validOrder.setStatus(OrderStatusEnum.READY);

            // Act & Assert
            assertThrows(InvalidSecurityPinException.class,
                () -> orderUseCase.deliverOrder(validOrder.getId(), null));
        }

        @Test
        @DisplayName("Edge Case: Estado vacío debe listar todos")
        void shouldListAllOrdersWithEmptyStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByRestaurant(RESTAURANT_ID, pageable)).thenReturn(orderPage);

            // Act
            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant("", pageable);

            // Assert
            verify(orderPersistencePort).listOrdersByRestaurant(RESTAURANT_ID, pageable);
        }
    }
}

