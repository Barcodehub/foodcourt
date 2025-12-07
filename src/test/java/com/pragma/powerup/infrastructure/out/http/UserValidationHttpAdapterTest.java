package com.pragma.powerup.infrastructure.out.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.infrastructure.exception.RemoteServiceException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Pruebas unitarias con WireMock para UserValidationHttpAdapter
 * Valida comunicación HTTP con el microservicio de usuarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserValidationHttpAdapter - Tests con WireMock")
class UserValidationHttpAdapterTest {

    private WireMockServer wireMockServer;
    private UserValidationHttpAdapter adapter;
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    @Mock
    private HttpServletRequest httpServletRequest;

    private static final String BASE_PATH = "/users";

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(8089);
        wireMockServer.start();
        WireMock.configureFor("localhost", 8089);

        restTemplate = new RestTemplate();
        objectMapper = new ObjectMapper();
        adapter = new UserValidationHttpAdapter(restTemplate, httpServletRequest);

        // Usar reflection para establecer la URL del servicio
        try {
            java.lang.reflect.Field field = UserValidationHttpAdapter.class.getDeclaredField("usersServiceUrl");
            field.setAccessible(true);
            field.set(adapter, "http://localhost:8089");
        } catch (Exception e) {
            fail("No se pudo configurar usersServiceUrl: " + e.getMessage());
        }
    }

    @AfterEach
    void tearDown() {
        wireMockServer.stop();
    }

    @Nested
    @DisplayName("getUserById - Obtener usuario por ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Happy Path: Debe obtener usuario exitosamente")
        void shouldGetUserSuccessfully() throws Exception {
            // Arrange
            Long userId = 1L;
            Map<String, Object> roleData = Map.of(
                "id", 2,
                "name", "PROPIETARIO",
                "description", "Owner"
            );

            Map<String, Object> userData = Map.of(
                "id", 1,
                "name", "John",
                "lastName", "Doe",
                "email", "john@example.com",
                "phoneNumber", "+573001234567",
                "role", roleData,
                "restaurantWorkId", 10
            );

            Map<String, Object> response = Map.of("data", userData);
            String jsonResponse = objectMapper.writeValueAsString(response);

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(jsonResponse)));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

            // Act
            Optional<UserResponseModel> result = adapter.getUserById(userId);

            // Assert
            assertTrue(result.isPresent());
            assertEquals("John", result.get().getName());
            assertEquals("Doe", result.get().getLastName());
            assertEquals("PROPIETARIO", result.get().getRole());
            assertEquals(10L, result.get().getRestaurantWorkId());

            verify(getRequestedFor(urlEqualTo(BASE_PATH + "/" + userId))
                .withHeader("Authorization", equalTo("Bearer token123")));
        }

        @Test
        @DisplayName("Validación: Debe manejar usuario no encontrado (404)")
        void shouldHandleUserNotFound() {
            // Arrange
            Long userId = 999L;

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(404)
                    .withHeader("Content-Type", "application/json")
                    .withBody("{\"message\": \"Usuario no encontrado\"}")));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

            // Act & Assert
            assertThrows(RemoteServiceException.class, () -> adapter.getUserById(userId));
        }

        @Test
        @DisplayName("Error: Debe manejar error del servidor (500)")
        void shouldHandleServerError() {
            // Arrange
            Long userId = 1L;

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(500)
                    .withBody("Internal Server Error")));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

            // Act & Assert
            assertThrows(RemoteServiceException.class, () -> adapter.getUserById(userId));
        }

        @Test
        @DisplayName("Error: Debe manejar timeout de conexión")
        void shouldHandleTimeout() {
            // Arrange
            Long userId = 1L;

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withFixedDelay(5000))); // 5 segundos de delay

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token123");

            // Act & Assert
            assertThrows(Exception.class, () -> {
                adapter.getUserById(userId);
            });
        }

        @Test
        @DisplayName("Validación: Debe enviar token de autorización en el header")
        void shouldSendAuthorizationToken() throws Exception {
            // Arrange
            Long userId = 1L;
            String token = "Bearer mySecureToken123";

            Map<String, Object> response = Map.of(
                "data", Map.of(
                    "id", 1,
                    "name", "Test",
                    "lastName", "User",
                    "email", "test@example.com",
                    "role", Map.of("name", "CLIENTE")
                )
            );

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(response))));

            when(httpServletRequest.getHeader("Authorization")).thenReturn(token);

            // Act
            adapter.getUserById(userId);

            // Assert
            verify(getRequestedFor(urlEqualTo(BASE_PATH + "/" + userId))
                .withHeader("Authorization", equalTo(token)));
        }

        @Test
        @DisplayName("Edge Case: Debe funcionar sin token de autorización")
        void shouldWorkWithoutAuthorizationToken() throws Exception {
            // Arrange
            Long userId = 1L;

            Map<String, Object> response = Map.of(
                "data", Map.of(
                    "id", 1,
                    "name", "Test",
                    "role", Map.of("name", "CLIENTE")
                )
            );

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(response))));

            when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

            // Act
            Optional<UserResponseModel> result = adapter.getUserById(userId);

            // Assert
            assertTrue(result.isPresent());
            verify(getRequestedFor(urlEqualTo(BASE_PATH + "/" + userId))
                .withoutHeader("Authorization"));
        }
    }

    @Nested
    @DisplayName("isUserOwner - Validar rol de propietario")
    class IsUserOwnerTests {

        @Test
        @DisplayName("Happy Path: Debe validar que usuario es propietario")
        void shouldValidateUserIsOwner() throws Exception {
            // Arrange
            Long userId = 1L;

            Map<String, Object> response = Map.of(
                "data", Map.of(
                    "id", 1,
                    "name", "Owner",
                    "role", Map.of("name", "PROPIETARIO")
                )
            );

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(response))));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act
            boolean result = adapter.isUserOwner(userId);

            // Assert
            assertTrue(result);
        }

        @Test
        @DisplayName("Validación: Debe retornar false si usuario no es propietario")
        void shouldReturnFalseWhenUserIsNotOwner() throws Exception {
            // Arrange
            Long userId = 1L;

            Map<String, Object> response = Map.of(
                "data", Map.of(
                    "id", 1,
                    "name", "Client",
                    "role", Map.of("name", "CLIENTE")
                )
            );

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(response))));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act
            boolean result = adapter.isUserOwner(userId);

            // Assert
            assertFalse(result);
        }

        @Test
        @DisplayName("Error: Debe retornar false si hay error de conexión")
        void shouldReturnFalseOnConnectionError() {
            // Arrange
            Long userId = 1L;

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(500)));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act & Assert
            assertThrows(RemoteServiceException.class, () -> adapter.isUserOwner(userId));
        }
    }

    @Nested
    @DisplayName("Edge Cases y Escenarios de Error")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Respuesta con estructura inesperada")
        void shouldHandleUnexpectedResponseStructure() {
            // Arrange
            Long userId = 1L;
            String invalidJson = "{\"wrongKey\": \"value\"}";

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(invalidJson)));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act
            Optional<UserResponseModel> result = adapter.getUserById(userId);

            // Assert
            assertFalse(result.isPresent());
        }

        @Test
        @DisplayName("Edge Case: Respuesta JSON inválida")
        void shouldHandleInvalidJson() {
            // Arrange
            Long userId = 1L;
            String invalidJson = "{invalid json}";

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(invalidJson)));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act & Assert
            assertThrows(Exception.class, () -> adapter.getUserById(userId));
        }

        @Test
        @DisplayName("Edge Case: Usuario con restaurantWorkId null")
        void shouldHandleNullRestaurantWorkId() throws Exception {
            // Arrange
            Long userId = 1L;

            Map<String, Object> response = Map.of(
                "data", Map.of(
                    "id", 1,
                    "name", "Employee",
                    "role", Map.of("name", "EMPLEADO")
                    // restaurantWorkId es null
                )
            );

            stubFor(get(urlEqualTo(BASE_PATH + "/" + userId))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", "application/json")
                    .withBody(objectMapper.writeValueAsString(response))));

            when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer token");

            // Act
            Optional<UserResponseModel> result = adapter.getUserById(userId);

            // Assert
            assertTrue(result.isPresent());
            assertNull(result.get().getRestaurantWorkId());
        }
    }
}

