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

@ExtendWith(MockitoExtension.class)
@DisplayName("MetricsUseCase - M\u00e9tricas y Eficiencia")
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
    @DisplayName("HU18: Consultar m\u00e9tricas de duraci\u00f3n de pedidos")
    class GetOrdersDurationMetricsTests {

        @Test
        @DisplayName("Happy Path: Debe obtener m\u00e9tricas de duraci\u00f3n de pedidos")
        void shouldGetOrdersDurationMetrics() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                restaurantId,
                startDate,
                endDate,
                0,
                20,
                "durationMinutes",
                "DESC"
            )).thenReturn(expectedResponse);

            OrdersDurationMetricsResponseDto result = metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            assertNotNull(result);
            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validaci\u00f3n: Debe usar valores predeterminados de paginaci\u00f3n")
        void shouldUseDefaultPaginationValues() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), eq(0), eq(20), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Error: Debe permitir paginaci\u00f3n personalizada")
        void shouldAllowCustomPagination() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                eq(restaurantId), any(), any(), eq(2), eq(50), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 2, 50, null, null
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 2, 50, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validaci\u00f3n: Debe permitir ordenamiento personalizado")
        void shouldAllowCustomSorting() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), eq("orderId"), eq("ASC")
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, null, null, "orderId", "ASC"
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 20, "orderId", "ASC"
            );
        }
    }

    @Nested
    @DisplayName("HU18: Consultar m\u00e9tricas de eficiencia de empleados")
    class GetEmployeeEfficiencyMetricsTests {

        @Test
        @DisplayName("Happy Path: Debe obtener m\u00e9tricas de eficiencia de empleados")
        void shouldGetEmployeeEfficiencyMetrics() {
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                restaurantId,
                startDate,
                endDate,
                0,
                20,
                "averageDurationMinutes",
                "ASC"
            )).thenReturn(expectedResponse);

            EmployeeEfficiencyMetricsResponseDto result = metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            assertNotNull(result);
            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Validaci\u00f3n: Debe usar valores predeterminados correctos")
        void shouldUseCorrectDefaultValues() {
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), eq(0), eq(20), eq("averageDurationMinutes"), eq("ASC")
            )).thenReturn(expectedResponse);

            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, null
            );

            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Error: Debe permitir ordenar de forma descendente")
        void shouldAllowDescendingOrder() {
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), eq("DESC")
            )).thenReturn(expectedResponse);

            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, null, "DESC"
            );

            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "averageDurationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Validaci\u00f3n: Debe permitir ordenar por totalOrders")
        void shouldAllowSortByTotalOrders() {
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), eq("totalOrders"), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, null, "totalOrders", "DESC"
            );

            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 20, "totalOrders", "DESC"
            );
        }
    }

    @Nested
    @DisplayName("Edge Cases y Validaciones")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Debe manejar p\u00e1gina 0 correctamente")
        void shouldHandlePageZeroCorrectly() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), eq(0), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 10, null, null
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, startDate, endDate, 0, 10, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar tama\u00f1os de p\u00e1gina grandes")
        void shouldHandleLargePageSizes() {
            EmployeeEfficiencyMetricsResponseDto expectedResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), eq(100), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, null, 100, null, null
            );

            verify(orderAuditPort).getEmployeeEfficiencyMetrics(
                restaurantId, startDate, endDate, 0, 100, "averageDurationMinutes", "ASC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar rangos de fechas amplios")
        void shouldHandleWideDateRanges() {
            OffsetDateTime veryOldDate = OffsetDateTime.now().minusYears(1);
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), eq(veryOldDate), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                restaurantId, veryOldDate, endDate, null, null, null, null
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                restaurantId, veryOldDate, endDate, 0, 20, "durationMinutes", "DESC"
            );
        }

        @Test
        @DisplayName("Edge Case: Debe manejar m\u00faltiples llamadas consecutivas")
        void shouldHandleMultipleConsecutiveCalls() {
            OrdersDurationMetricsResponseDto durationResponse = new OrdersDurationMetricsResponseDto();
            EmployeeEfficiencyMetricsResponseDto efficiencyResponse = new EmployeeEfficiencyMetricsResponseDto();

            when(orderAuditPort.getOrdersDurationMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(durationResponse);

            when(orderAuditPort.getEmployeeEfficiencyMetrics(
                anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(efficiencyResponse);

            metricsUseCase.getOrdersDurationMetrics(restaurantId, startDate, endDate, null, null, null, null);
            metricsUseCase.getEmployeeEfficiencyMetrics(restaurantId, startDate, endDate, null, null, null, null);

            verify(orderAuditPort, times(1)).getOrdersDurationMetrics(anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
            verify(orderAuditPort, times(1)).getEmployeeEfficiencyMetrics(anyLong(), any(), any(), anyInt(), anyInt(), anyString(), anyString());
        }

        @Test
        @DisplayName("Validaci\u00f3n: Debe delegar correctamente al puerto de auditor\u00eda")
        void shouldDelegateCorrectlyToAuditPort() {
            OrdersDurationMetricsResponseDto expectedResponse = new OrdersDurationMetricsResponseDto();
            Long specificRestaurantId = 42L;

            when(orderAuditPort.getOrdersDurationMetrics(
                eq(specificRestaurantId), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            )).thenReturn(expectedResponse);

            metricsUseCase.getOrdersDurationMetrics(
                specificRestaurantId, startDate, endDate, null, null, null, null
            );

            verify(orderAuditPort).getOrdersDurationMetrics(
                eq(specificRestaurantId), any(), any(), anyInt(), anyInt(), anyString(), anyString()
            );
        }
    }
}
