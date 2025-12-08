package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.*;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderUseCase - Gestion de Pedidos")
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

    @Mock
    private IDishPersistencePort dishPersistencePort;

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

        // Crear platos para la orden
        DishModel dish1 = new DishModel();
        dish1.setId(1L);
        dish1.setName("Plato 1");
        dish1.setRestaurantId(RESTAURANT_ID);
        dish1.setActive(true);
        dish1.setPrice(10000);

        DishModel dish2 = new DishModel();
        dish2.setId(2L);
        dish2.setName("Plato 2");
        dish2.setRestaurantId(RESTAURANT_ID);
        dish2.setActive(true);
        dish2.setPrice(15000);

        OrderDishModel orderDish1 = new OrderDishModel();
        orderDish1.setDish(dish1);
        orderDish1.setQuantity(2);

        OrderDishModel orderDish2 = new OrderDishModel();
        orderDish2.setDish(dish2);
        orderDish2.setQuantity(1);

        validOrder = new OrderModel();
        validOrder.setId(1L);
        validOrder.setRestaurant(restaurant);
        validOrder.setClient(CLIENT_ID);
        validOrder.setStatus(OrderStatusEnum.PENDIENT);
        validOrder.setSecurityPin(SECURITY_PIN);
        validOrder.setDishes(Arrays.asList(orderDish1, orderDish2));
    }

    @Nested
    @DisplayName("HU11: Realizar pedido")
    class CreateOrderTests {

        @Test
        @DisplayName("Happy Path: Debe crear pedido correctamente")
        void shouldCreateOrderSuccessfully() {
            // Mockear la validación de platos
            DishModel dish1 = validOrder.getDishes().get(0).getDish();
            DishModel dish2 = validOrder.getDishes().get(1).getDish();
            when(dishPersistencePort.findById(1L)).thenReturn(Optional.of(dish1));
            when(dishPersistencePort.findById(2L)).thenReturn(Optional.of(dish2));

            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.empty());
            when(smsUseCase.generateSecurityPin()).thenReturn(SECURITY_PIN);
            when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenReturn(validOrder);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            doNothing().when(orderAuditPort).registerStatusChange(any());

            OrderModel result = orderUseCase.createOrder(validOrder);

            assertNotNull(result);
            assertEquals(OrderStatusEnum.PENDIENT, result.getStatus());
            assertNotNull(result.getSecurityPin());
            verify(orderPersistencePort).saveOrder(any(OrderModel.class));
            verify(orderAuditPort).registerStatusChange(any());
        }

        @Test
        @DisplayName("Validacion: Debe asignar estado PENDIENT automaticamente")
        void shouldSetPendingStatusAutomatically() {
            validOrder.setStatus(null);

            // Mockear la validación de platos
            DishModel dish1 = validOrder.getDishes().get(0).getDish();
            DishModel dish2 = validOrder.getDishes().get(1).getDish();
            when(dishPersistencePort.findById(1L)).thenReturn(Optional.of(dish1));
            when(dishPersistencePort.findById(2L)).thenReturn(Optional.of(dish2));

            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.empty());
            when(smsUseCase.generateSecurityPin()).thenReturn(SECURITY_PIN);
            when(orderPersistencePort.saveOrder(any(OrderModel.class))).thenReturn(validOrder);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            doNothing().when(orderAuditPort).registerStatusChange(any());

            orderUseCase.createOrder(validOrder);

            verify(orderPersistencePort).saveOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.PENDIENT
            ));
        }

        @Test
        @DisplayName("Error: Debe rechazar si el cliente ya tiene un pedido activo")
        void shouldRejectWhenClientHasActiveOrder() {
            OrderModel activeOrder = new OrderModel();
            activeOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(orderPersistencePort.getActiveOrderByUserId(CLIENT_ID)).thenReturn(Optional.of(activeOrder));

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
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByStatusAndRestaurant(
                OrderStatusEnum.PENDIENT, RESTAURANT_ID, pageable
            )).thenReturn(orderPage);

            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant("PENDIENT", pageable);

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            verify(orderPersistencePort).listOrdersByStatusAndRestaurant(OrderStatusEnum.PENDIENT, RESTAURANT_ID, pageable);
        }

        @Test
        @DisplayName("Validacion: Debe listar todos los pedidos sin filtro de estado")
        void shouldListAllOrdersWhenNoStatusFilter() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByRestaurant(RESTAURANT_ID, pageable)).thenReturn(orderPage);

            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant(null, pageable);

            assertNotNull(result);
            verify(orderPersistencePort).listOrdersByRestaurant(RESTAURANT_ID, pageable);
        }

        @Test
        @DisplayName("Error: Debe rechazar si el empleado no tiene restaurante asignado")
        void shouldRejectWhenEmployeeHasNoRestaurant() {
            employee.setRestaurantWorkId(null);
            Pageable pageable = PageRequest.of(0, 10);

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(RestaurantNotFoundException.class,
                () -> orderUseCase.listOrdersByStatusAndRestaurant("PENDIENT", pageable));
        }
    }

    @Nested
    @DisplayName("HU13: Asignarse a un pedido y cambiar estado a 'en preparacion'")
    class AssignOrderTests {

        @Test
        @DisplayName("Happy Path: Debe asignar pedido a empleado correctamente")
        void shouldAssignOrderToEmployeeSuccessfully() {
            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());

            OrderModel result = orderUseCase.assignOrderToEmployee(validOrder.getId());

            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getEmployee().equals(EMPLOYEE_ID) &&
                order.getStatus() == OrderStatusEnum.IN_PREPARE
            ));
            verify(orderAuditPort).registerStatusChange(any());
        }

        @Test
        @DisplayName("Validacion: Debe rechazar asignacion si pedido no esta PENDIENT")
        void shouldRejectAssignmentWhenOrderNotPending() {
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            Long orderId = validOrder.getId();
            assertThrows(InvalidOrderStatusException.class, () -> {
                orderUseCase.assignOrderToEmployee(orderId);
            });
            verify(orderPersistencePort, never()).updateOrder(any());
        }

        @Test
        @DisplayName("Error: Debe rechazar si empleado no pertenece al restaurante")
        void shouldRejectWhenEmployeeNotFromRestaurant() {
            employee.setRestaurantWorkId(999L);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.assignOrderToEmployee(orderId));
        }
    }

    @Nested
    @DisplayName("HU14: Notificar que el pedido esta listo")
    class MarkOrderAsReadyTests {

        @Test
        @DisplayName("Happy Path: Debe marcar pedido como listo y enviar SMS")
        void shouldMarkOrderAsReadyAndSendSms() {
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());
            doNothing().when(smsUseCase).sendOrderReadyNotification(any(), any());

            OrderModel result = orderUseCase.markOrderAsReady(validOrder.getId());

            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.READY
            ));
            verify(smsUseCase).sendOrderReadyNotification(any(UserResponseModel.class), any(OrderModel.class));
        }

        @Test
        @DisplayName("Validacion: Debe rechazar si pedido no esta EN PREPARACION")
        void shouldRejectWhenOrderNotInPrepare() {
            validOrder.setStatus(OrderStatusEnum.PENDIENT);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.markOrderAsReady(orderId));
        }

        @Test
        @DisplayName("Error: Debe rechazar si cliente no tiene telefono")
        void shouldRejectWhenClientHasNoPhone() {
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            validOrder.setEmployee(EMPLOYEE_ID);
            client.setPhoneNumber(null);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));

            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.markOrderAsReady(orderId));
        }
    }

    @Nested
    @DisplayName("HU15: Entregar pedido")
    class DeliverOrderTests {

        @Test
        @DisplayName("Happy Path: Debe entregar pedido con PIN correcto")
        void shouldDeliverOrderWithCorrectPin() {
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(EMPLOYEE_ID);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());

            OrderModel result = orderUseCase.deliverOrder(validOrder.getId(), SECURITY_PIN);

            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.DELIVERED
            ));
        }

        @Test
        @DisplayName("Validacion: Debe rechazar PIN incorrecto")
        void shouldRejectIncorrectPin() {
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(EMPLOYEE_ID);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(InvalidSecurityPinException.class,
                () -> orderUseCase.deliverOrder(orderId, "wrongPIN"));
        }

        @Test
        @DisplayName("Error: Debe rechazar si pedido no esta LISTO")
        void shouldRejectWhenOrderNotReady() {
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);
            validOrder.setEmployee(EMPLOYEE_ID);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(InvalidOrderStatusException.class,
                () -> orderUseCase.deliverOrder(orderId, SECURITY_PIN));
        }

        @Test
        @DisplayName("Validacion: Debe rechazar si empleado no es el asignado")
        void shouldRejectWhenEmployeeNotAssigned() {
            validOrder.setStatus(OrderStatusEnum.READY);
            validOrder.setEmployee(999L);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));

            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.deliverOrder(orderId, SECURITY_PIN));
        }

        @Test
        @DisplayName("Error: Debe rechazar PIN vacio")
        void shouldRejectEmptyPin() {
            Long orderId = validOrder.getId();

            assertThrows(InvalidSecurityPinException.class,
                () -> orderUseCase.deliverOrder(orderId, ""));
        }
    }

    @Nested
    @DisplayName("HU16: Cancelar pedido")
    class CancelOrderTests {

        @Test
        @DisplayName("Happy Path: Debe cancelar pedido PENDIENT correctamente")
        void shouldCancelPendingOrderSuccessfully() {
            validOrder.setStatus(OrderStatusEnum.PENDIENT);

            when(orderPersistencePort.findById(validOrder.getId())).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);
            when(userValidationPort.getUserById(CLIENT_ID)).thenReturn(Optional.of(client));
            when(orderPersistencePort.updateOrder(any(OrderModel.class))).thenReturn(validOrder);
            doNothing().when(orderAuditPort).registerStatusChange(any());
            doNothing().when(smsUseCase).sendOrderCancelledNotification(any(), any());

            OrderModel result = orderUseCase.cancelOrder(validOrder.getId());

            assertNotNull(result);
            verify(orderPersistencePort).updateOrder(argThat(order ->
                order.getStatus() == OrderStatusEnum.CANCELLED
            ));
            verify(smsUseCase).sendOrderCancelledNotification(any(), any());
        }

        @Test
        @DisplayName("Validacion: Debe rechazar cancelacion si no es el cliente dueño")
        void shouldRejectCancellationByNonOwner() {
            validOrder.setClient(999L);
            Long orderId = validOrder.getId();

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);

            assertThrows(UnauthorizedOperationException.class,
                () -> orderUseCase.cancelOrder(orderId));
        }

        @Test
        @DisplayName("Error: Debe rechazar cancelacion si pedido no esta PENDIENT")
        void shouldRejectCancellationWhenOrderNotPending() {
            Long orderId = validOrder.getId();
            validOrder.setStatus(OrderStatusEnum.IN_PREPARE);

            when(orderPersistencePort.findById(orderId)).thenReturn(Optional.of(validOrder));
            when(securityContextPort.getCurrentUserId()).thenReturn(CLIENT_ID);

            assertThrows(OrderCancellationException.class,
                () -> orderUseCase.cancelOrder(orderId));
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Pedido inexistente debe lanzar excepcion")
        void shouldThrowExceptionForNonExistentOrder() {
            when(orderPersistencePort.findById(999L)).thenReturn(Optional.empty());

            assertThrows(OrderNotFoundException.class,
                () -> orderUseCase.assignOrderToEmployee(999L));
        }

        @Test
        @DisplayName("Edge Case: PIN null debe ser rechazado")
        void shouldRejectNullPin() {
            validOrder.setStatus(OrderStatusEnum.READY);

            Long orderId = validOrder.getId();
            assertThrows(InvalidSecurityPinException.class, () -> {
                orderUseCase.deliverOrder(orderId, null);
            });
        }

        @Test
        @DisplayName("Edge Case: Estado vacio debe listar todos")
        void shouldListAllOrdersWithEmptyStatus() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<OrderModel> orderPage = new PageImpl<>(Arrays.asList(validOrder));

            when(securityContextPort.getCurrentUserId()).thenReturn(EMPLOYEE_ID);
            when(userValidationPort.getUserById(EMPLOYEE_ID)).thenReturn(Optional.of(employee));
            when(orderPersistencePort.listOrdersByRestaurant(RESTAURANT_ID, pageable)).thenReturn(orderPage);

            Page<OrderModel> result = orderUseCase.listOrdersByStatusAndRestaurant("", pageable);

            assertNotNull(result);
            verify(orderPersistencePort).listOrdersByRestaurant(RESTAURANT_ID, pageable);
        }
    }
}
