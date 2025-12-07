package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.exception.InvalidRestaurantException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotFoundException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RestaurantUseCase - Gestion de Restaurantes")
class RestaurantUseCaseTest {

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private IUserValidationPort userValidationPort;

    @InjectMocks
    private RestaurantUseCase restaurantUseCase;

    private RestaurantModel validRestaurant;
    private UserResponseModel ownerUser;

    @BeforeEach
    void setUp() {
        validRestaurant = new RestaurantModel();
        validRestaurant.setName("Restaurante Prueba");
        validRestaurant.setNit("900123456");
        validRestaurant.setAddress("Calle 123 #45-67");
        validRestaurant.setPhoneNumber("+573001234567");
        validRestaurant.setUrlLogo("https://example.com/logo.png");
        validRestaurant.setOwnerId(1L);

        ownerUser = new UserResponseModel();
        ownerUser.setId(1L);
        ownerUser.setName("John");
        ownerUser.setLastName("Doe");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setRole("PROPIETARIO");
    }

    @Nested
    @DisplayName("HU2: Crear Restaurante")
    class CreateRestaurantTests {

        @Test
        @DisplayName("Happy Path: Debe crear restaurante con datos válidos")
        void shouldCreateRestaurantWithValidData() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(validRestaurant.getOwnerId())).thenReturn(true);
            when(restaurantPersistencePort.saveRestaurant(any(RestaurantModel.class))).thenReturn(validRestaurant);

            RestaurantModel result = restaurantUseCase.createRestaurant(validRestaurant);

            assertNotNull(result);
            assertEquals(validRestaurant.getName(), result.getName());
            assertEquals(validRestaurant.getNit(), result.getNit());

            verify(restaurantPersistencePort).findByNit(validRestaurant.getNit());
            verify(userValidationPort).getUserById(validRestaurant.getOwnerId());
            verify(userValidationPort).isUserOwner(validRestaurant.getOwnerId());
            verify(restaurantPersistencePort).saveRestaurant(validRestaurant);
        }

        @Test
        @DisplayName("Validacion: Debe rechazar nombre de restaurante numerico")
        void shouldRejectNumericRestaurantName() {
            validRestaurant.setName("123456");

            assertThrows(InvalidRestaurantException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort, never()).saveRestaurant(any(RestaurantModel.class));
        }

        @Test
        @DisplayName("Error: Debe rechazar restaurante con NIT duplicado")
        void shouldRejectDuplicateNit() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.of(validRestaurant));

            assertThrows(RestaurantAlreadyExistsException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort, never()).saveRestaurant(any(RestaurantModel.class));
        }
    }

    @Nested
    @DisplayName("Validaciones de Propietario")
    class OwnerValidationTests {

        @Test
        @DisplayName("Validacion: Debe rechazar propietario inexistente")
        void shouldRejectNonExistentOwner() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort, never()).saveRestaurant(any(RestaurantModel.class));
        }

        @Test
        @DisplayName("Validacion: Debe rechazar usuario sin rol propietario")
        void shouldRejectUserWithoutOwnerRole() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(validRestaurant.getOwnerId())).thenReturn(false);

            assertThrows(UserNotOwnerException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort, never()).saveRestaurant(any(RestaurantModel.class));
        }

        @Test
        @DisplayName("Error: Debe validar que el propietario existe antes de verificar rol")
        void shouldValidateOwnerExistsBeforeCheckingRole() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(userValidationPort, never()).isUserOwner(anyLong());
        }
    }

    @Nested
    @DisplayName("Validaciones de Nombre")
    class NameValidationTests {

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "  ", "\t", "\n"})
        @DisplayName("Validacion: Debe rechazar nombres invalidos (null, vacio, solo espacios)")
        void shouldRejectInvalidNames(String invalidName) {
            validRestaurant.setName(invalidName);

            assertThrows(InvalidRestaurantException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort, never()).saveRestaurant(any(RestaurantModel.class));
        }

        @Test
        @DisplayName("Happy Path: Debe aceptar nombre alfanumerico")
        void shouldAcceptAlphanumericName() {
            validRestaurant.setName("Restaurante 123 ABC");

            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(validRestaurant.getOwnerId())).thenReturn(true);
            when(restaurantPersistencePort.saveRestaurant(any(RestaurantModel.class))).thenReturn(validRestaurant);

            RestaurantModel result = restaurantUseCase.createRestaurant(validRestaurant);

            assertNotNull(result);
            verify(restaurantPersistencePort).saveRestaurant(validRestaurant);
        }

        @Test
        @DisplayName("Happy Path: Debe aceptar nombre con caracteres especiales")
        void shouldAcceptNameWithSpecialCharacters() {
            validRestaurant.setName("Restaurante D'Amico");

            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(validRestaurant.getOwnerId())).thenReturn(true);
            when(restaurantPersistencePort.saveRestaurant(any(RestaurantModel.class))).thenReturn(validRestaurant);

            RestaurantModel result = restaurantUseCase.createRestaurant(validRestaurant);

            assertNotNull(result);
            verify(restaurantPersistencePort).saveRestaurant(validRestaurant);
        }
    }

    @Nested
    @DisplayName("HU9: Listar los restaurantes")
    class ListRestaurantsTests {

        @Test
        @DisplayName("Happy Path: Debe listar restaurantes con paginacion")
        void shouldListRestaurantsWithPagination() {
            RestaurantModel restaurant1 = new RestaurantModel(1L, "Restaurant 1", "900111111", "Address 1", "+573001111111", "logo1.png", 1L);
            RestaurantModel restaurant2 = new RestaurantModel(2L, "Restaurant 2", "900222222", "Address 2", "+573002222222", "logo2.png", 2L);
            RestaurantModel restaurant3 = new RestaurantModel(3L, "Restaurant 3", "900333333", "Address 3", "+573003333333", "logo3.png", 3L);

            List<RestaurantModel> restaurants = Arrays.asList(restaurant1, restaurant2, restaurant3);
            Pageable pageable = PageRequest.of(0, 10);
            Page<RestaurantModel> restaurantPage = new PageImpl<>(restaurants, pageable, restaurants.size());

            when(restaurantPersistencePort.findAll(pageable)).thenReturn(restaurantPage);

            Page<RestaurantModel> result = restaurantUseCase.listRestaurants(pageable);

            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
            assertEquals(3, result.getContent().size());
            assertEquals("Restaurant 1", result.getContent().get(0).getName());

            verify(restaurantPersistencePort).findAll(pageable);
        }

        @Test
        @DisplayName("Validacion: Debe retornar pagina vacia cuando no hay restaurantes")
        void shouldReturnEmptyPageWhenNoRestaurants() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<RestaurantModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(restaurantPersistencePort.findAll(pageable)).thenReturn(emptyPage);

            Page<RestaurantModel> result = restaurantUseCase.listRestaurants(pageable);

            assertNotNull(result);
            assertEquals(0, result.getTotalElements());
            assertTrue(result.getContent().isEmpty());

            verify(restaurantPersistencePort).findAll(pageable);
        }

        @Test
        @DisplayName("Validacion: Debe manejar diferentes tamanos de pagina")
        void shouldHandleDifferentPageSizes() {
            RestaurantModel restaurant1 = new RestaurantModel(1L, "Restaurant 1", "900111111", "Address 1", "+573001111111", "logo1.png", 1L);
            RestaurantModel restaurant2 = new RestaurantModel(2L, "Restaurant 2", "900222222", "Address 2", "+573002222222", "logo2.png", 2L);

            List<RestaurantModel> restaurants = Arrays.asList(restaurant1, restaurant2);
            Pageable pageable = PageRequest.of(0, 5);
            Page<RestaurantModel> restaurantPage = new PageImpl<>(restaurants, pageable, 10);

            when(restaurantPersistencePort.findAll(pageable)).thenReturn(restaurantPage);

            Page<RestaurantModel> result = restaurantUseCase.listRestaurants(pageable);

            assertNotNull(result);
            assertEquals(10, result.getTotalElements());
            assertEquals(2, result.getContent().size());
            assertTrue(result.hasNext());

            verify(restaurantPersistencePort).findAll(pageable);
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Debe manejar NIT con diferentes formatos")
        void shouldHandleDifferentNitFormats() {
            validRestaurant.setNit("900-123-456-7");

            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(validRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(validRestaurant.getOwnerId())).thenReturn(true);
            when(restaurantPersistencePort.saveRestaurant(any(RestaurantModel.class))).thenReturn(validRestaurant);

            RestaurantModel result = restaurantUseCase.createRestaurant(validRestaurant);

            assertNotNull(result);
            assertEquals("900-123-456-7", result.getNit());
        }

        @Test
        @DisplayName("Edge Case: Debe validar orden de operaciones correcto")
        void shouldFollowCorrectOperationOrder() {
            when(restaurantPersistencePort.findByNit(validRestaurant.getNit())).thenReturn(Optional.of(validRestaurant));

            assertThrows(RestaurantAlreadyExistsException.class,
                    () -> restaurantUseCase.createRestaurant(validRestaurant));

            verify(restaurantPersistencePort).findByNit(validRestaurant.getNit());
            verify(userValidationPort, never()).getUserById(anyLong());
            verify(userValidationPort, never()).isUserOwner(anyLong());
        }

        @Test
        @DisplayName("Edge Case: Debe permitir mismo propietario para multiples restaurantes")
        void shouldAllowSameOwnerForMultipleRestaurants() {
            RestaurantModel secondRestaurant = new RestaurantModel();
            secondRestaurant.setName("Segundo Restaurante");
            secondRestaurant.setNit("900999999");
            secondRestaurant.setAddress("Otra Calle");
            secondRestaurant.setPhoneNumber("+573009999999");
            secondRestaurant.setUrlLogo("https://example.com/logo2.png");
            secondRestaurant.setOwnerId(1L);

            when(restaurantPersistencePort.findByNit(secondRestaurant.getNit())).thenReturn(Optional.empty());
            when(userValidationPort.getUserById(secondRestaurant.getOwnerId())).thenReturn(Optional.of(ownerUser));
            when(userValidationPort.isUserOwner(secondRestaurant.getOwnerId())).thenReturn(true);
            when(restaurantPersistencePort.saveRestaurant(any(RestaurantModel.class))).thenReturn(secondRestaurant);

            RestaurantModel result = restaurantUseCase.createRestaurant(secondRestaurant);

            assertNotNull(result);
            assertEquals(1L, result.getOwnerId());
            verify(restaurantPersistencePort).saveRestaurant(secondRestaurant);
        }
    }
}
