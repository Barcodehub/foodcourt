package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.apifirst.model.EmployeeEfficiencyMetricsResponseDto;
import com.pragma.powerup.apifirst.model.OrdersDurationMetricsResponseDto;
import com.pragma.powerup.domain.spi.IOrderAuditPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para MetricsUseCase
 * Cubre la siguiente Historia de Usuario:
 * - HU18: Consultar la eficiencia de los pedidos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsUseCase - Métricas y Eficiencia")
class MetricsUseCaseTest {

    @Mock
    private IOrderAuditPort orderAuditPort;

    @InjectMocks
    private MetricsUseCase metricsUseCase;

    private Long restaurantId;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;

    @BeforeEach
    void setUp() {
        restaurantId = 1L;
        startDate = OffsetDateTime.now().minusDays(30);
        endDate = OffsetDateTime.now();
    }

    @Nested
    @DisplayName("HU18: Consultar métricas de duración de pedidos")
    class GetOrdersDurationMetricsTests {

        @Test
        @DisplayName("Happy Path: Debe obtener métricas de duración de pedidos")
        void shouldGetOrdersDurationMetrics() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                eq(restaurantId),
                eq(startDate),
                eq(endDate),
                eq(0),
                eq(20),
                eq("durationMinutes"),
                eq("DESC")
            )).thenReturn(expectedResponse);

            // Act
            OrdersDurationMetricsResponseDto result = metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            // Assert
            assertNotNull(result);
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validación: Debe usar valores predeterminados de paginación")
        void shouldUseDefaultPaginationValues() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), eq(0), eq(20), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Error: Debe permitir paginación personalizada")
        void shouldAllowCustomPagination() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                eq(restaurantId), any(), any(), eq(2), eq(50), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 2, 50, null, null
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 2, 50, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validación: Debe permitir ordenamiento personalizado")
        void shouldAllowCustomSorting() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), eq("orderId"), eq("ASC")
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, "orderId", "ASC"
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "orderId", "ASC"
            );
        }
    }

    @Nested
    @DisplayName("HU18: Consultar métricas de eficiencia de empleados")
    class GetEmployeeEfficiencyMetricsTests {

        @Test
        @DisplayName("Happy Path: Debe obtener métricas de eficiencia de empleados")
        void shouldGetEmployeeEfficiencyMetrics() {
            // Arrange
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                eq(restaurantId),
                eq(startDate),
                eq(endDate),
                eq(0),
                eq(20),
                eq("averageDurationMinutes"),
                eq("ASC")
            )).thenReturn(expectedResponse);

            // Act
            EmployeeEfficiencyMetricsResponseDto result = metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            // Assert
            assertNotNull(result);
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Validación: Debe usar valores predeterminados correctos")
        void shouldUseCorrectDefaultValues() {
            // Arrange
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), eq(0), eq(20), eq("averageDurationMinutes"), eq("ASC")
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            // Assert - ASC para mostrar los más eficientes primero
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Error: Debe permitir ordenar de forma descendente")
        void shouldAllowDescendingOrder() {
            // Arrange
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), eq("DESC")
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, "DESC"
            );

            // Assert
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validación: Debe permitir ordenar por totalOrders")
        void shouldAllowSortByTotalOrders() {
            // Arrange
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), eq("totalOrders"), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, "totalOrders", "DESC"
            );

            // Assert
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "totalOrders", "DESC"
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases y Validaciones")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Debe manejar página 0 correctamente")
        void shouldHandlePageZeroCorrectly() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), eq(0), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 10, null, null
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 10, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar tamaños de página grandes")
        void shouldHandleLargePageSizes() {
            // Arrange
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), eq(100), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, 100, null, null
            );

            // Assert
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 100, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar rangos de fechas amplios")
        void shouldHandleWideDateRanges() {
            // Arrange
            OffsetDateTime veryOldDate = OffsetDateTime.now().minusYears(1);
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), eq(veryOldDate), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, veryOldDate, endDate, null, null, null, null
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, veryOldDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar múltiples llamadas consecutivas")
        void shouldHandleMultipleConsecutiveCalls() {
            // Arrange
            OrdersDurationMetricsResponseDto durationResponse = new OrdersDurationMetricsResponseDto();
            EmployeeEfficiencyMetricsResponseDto efficiencyResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(durationResponse);

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(efficiencyResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(restaurantId, startDate, endDate, null, null, null, null);
            metricsUseCase.getEmployeeEfficiencyMetrics(restaurantId, startDate, endDate, null, null, null, null);

            // Assert
            verify(orderAuditPort, times(1)).getOrdersDurationMetrics(anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
            verify(orderAuditPort, times(1)).getEmployeeEfficiencyMetrics(anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("Validación: Debe delegar correctamente al puerto de auditoría")
        void shouldDelegateCorrectlyToAuditPort() {
            // Arrange
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();
            Long specificRestaurantId = 42L;

            when(orderAuditPort.getOrdersDurationMetrics(
                eq(specificRestaurantId), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            // Act
            metricsUseCase.getOrdersDurationMetrics(
                specificRestaurantId, startDate, endDate, null, null, null, null
            );

            // Assert
            verify(orderAuditPort).getOrdersDurationMetrics(
                eq(specificRestaurantId), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            );
        }
    }
}

