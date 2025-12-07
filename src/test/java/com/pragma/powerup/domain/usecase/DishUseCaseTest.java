package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UnauthorizedDishOperationException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias para DishUseCase
 * Cubre las siguientes Historias de Usuario:
 * - HU3: Crear Plato
 * - HU4: Modificar Plato
 * - HU7: Habilitar/Deshabilitar Plato
 * - HU10: Listar los platos de un restaurante
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DishUseCase - Gestión de Platos")
class DishUseCaseTest {

    @Mock
    private IDishPersistencePort dishPersistencePort;

    @Mock
    private IRestaurantPersistencePort restaurantPersistencePort;

    @Mock
    private ISecurityContextPort securityContextPort;

    @InjectMocks
    private DishUseCase dishUseCase;

    private DishModel validDish;
    private RestaurantModel restaurant;
    private final Long OWNER_ID = 1L;
    private final Long RESTAURANT_ID = 1L;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantModel();
        restaurant.setId(RESTAURANT_ID);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(OWNER_ID);

        validDish = new DishModel();
        validDish.setId(1L);
        validDish.setName("Bandeja Paisa");
        validDish.setPrice(25000);
        validDish.setDescription("Plato típico colombiano");
        validDish.setUrlImage("https://example.com/bandeja.jpg");
        validDish.setCategory(CategoryEnum.PLATOS_FUERTES);
        validDish.setActive(true);
        validDish.setRestaurantId(RESTAURANT_ID);
    }

    @Nested
    @DisplayName("HU3: Crear Plato")
    class CreateDishTests {

        @Test
        @DisplayName("Happy Path: Debe crear plato con datos válidos")
        void shouldCreateDishWithValidData() {
            // Arrange
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            DishModel result = dishUseCase.createDish(validDish);

            // Assert
            assertNotNull(result);
            assertEquals("Bandeja Paisa", result.getName());
            assertEquals(25000, result.getPrice());
            assertTrue(result.getActive());

            verify(restaurantPersistencePort).findById(RESTAURANT_ID);
            verify(securityContextPort).getCurrentUserId();
            verify(dishPersistencePort).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Validación: Debe activar el plato automáticamente al crearlo")
        void shouldAutomaticallyActivateDishOnCreation() {
            // Arrange
            validDish.setActive(null); // Sin estado inicial

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenAnswer(invocation -> {
                DishModel dish = invocation.getArgument(0);
                assertTrue(dish.getActive(), "El plato debe estar activo");
                return dish;
            });

            // Act
            dishUseCase.createDish(validDish);

            // Assert
            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getActive()));
        }

        @Test
        @DisplayName("Error: Debe rechazar creación si restaurante no existe")
        void shouldRejectCreationWhenRestaurantNotExists() {
            // Arrange
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.createDish(validDish));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Error: Debe rechazar creación por usuario no propietario")
        void shouldRejectCreationByNonOwner() {
            // Arrange
            Long differentOwnerId = 999L;

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(differentOwnerId);

            // Act & Assert
            assertThrows(UnauthorizedDishOperationException.class, () -> dishUseCase.createDish(validDish));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }
    }

    @Nested
    @DisplayName("HU4: Modificar Plato")
    class UpdateDishTests {

        @Test
        @DisplayName("Happy Path: Debe actualizar precio del plato")
        void shouldUpdateDishPrice() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(30000);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            DishModel result = dishUseCase.updateDish(validDish.getId(), updateData);

            // Assert
            assertNotNull(result);
            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getPrice() == 30000));
        }

        @Test
        @DisplayName("Happy Path: Debe actualizar descripción del plato")
        void shouldUpdateDishDescription() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setDescription("Nueva descripción actualizada");

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            dishUseCase.updateDish(validDish.getId(), updateData);

            // Assert
            verify(dishPersistencePort).saveDish(argThat(dish ->
                "Nueva descripción actualizada".equals(dish.getDescription())
            ));
        }

        @Test
        @DisplayName("Validación: Debe rechazar precio negativo o cero")
        void shouldRejectNegativeOrZeroPrice() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(0);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);

            // Act & Assert
            assertThrows(InvalidDishException.class, () -> dishUseCase.updateDish(validDish.getId(), updateData));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Error: Debe rechazar actualización de plato inexistente")
        void shouldRejectUpdateOfNonExistentDish() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(30000);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(DishNotFoundException.class, () -> dishUseCase.updateDish(validDish.getId(), updateData));
        }

        @Test
        @DisplayName("Validación: Debe ignorar descripción vacía en actualización")
        void shouldIgnoreEmptyDescriptionInUpdate() {
            // Arrange
            String originalDescription = validDish.getDescription();
            DishModel updateData = new DishModel();
            updateData.setDescription("");

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            dishUseCase.updateDish(validDish.getId(), updateData);

            // Assert - La descripción original debe mantenerse
            verify(dishPersistencePort).saveDish(argThat(dish ->
                originalDescription.equals(dish.getDescription())
            ));
        }

        @Test
        @DisplayName("Validación: Debe permitir actualización solo de precio")
        void shouldAllowPriceOnlyUpdate() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(35000);
            // No se actualiza descripción

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            dishUseCase.updateDish(validDish.getId(), updateData);

            // Assert
            verify(dishPersistencePort).saveDish(any(DishModel.class));
        }
    }

    @Nested
    @DisplayName("HU7: Habilitar/Deshabilitar Plato")
    class ToggleDishStatusTests {

        @Test
        @DisplayName("Happy Path: Debe deshabilitar plato activo")
        void shouldDisableActiveDish() {
            // Arrange
            validDish.setActive(true);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            DishModel result = dishUseCase.toggleDishStatus(validDish.getId());

            // Assert
            verify(dishPersistencePort).saveDish(argThat(dish -> !dish.getActive()));
        }

        @Test
        @DisplayName("Happy Path: Debe habilitar plato inactivo")
        void shouldEnableInactiveDish() {
            // Arrange
            validDish.setActive(false);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            dishUseCase.toggleDishStatus(validDish.getId());

            // Assert
            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getActive()));
        }

        @Test
        @DisplayName("Error: Debe rechazar toggle por usuario no autorizado")
        void shouldRejectToggleByUnauthorizedUser() {
            // Arrange
            Long differentOwnerId = 999L;

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(differentOwnerId);

            // Act & Assert
            assertThrows(UnauthorizedDishOperationException.class,
                () -> dishUseCase.toggleDishStatus(validDish.getId()));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }
    }

    @Nested
    @DisplayName("HU10: Listar los platos de un restaurante")
    class ListDishesTests {

        @Test
        @DisplayName("Happy Path: Debe listar platos de un restaurante")
        void shouldListDishesFromRestaurant() {
            // Arrange
            DishModel dish1 = createDish(1L, "Plato 1", CategoryEnum.PLATOS_FUERTES);
            DishModel dish2 = createDish(2L, "Plato 2", CategoryEnum.SOPAS);
            DishModel dish3 = createDish(3L, "Plato 3", CategoryEnum.POSTRES);

            List<DishModel> dishes = Arrays.asList(dish1, dish2, dish3);
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> dishPage = new PageImpl<>(dishes, pageable, dishes.size());

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(RESTAURANT_ID, null, pageable)).thenReturn(dishPage);

            // Act
            Page<DishModel> result = dishUseCase.listDishesByRestaurant(RESTAURANT_ID, null, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
            assertEquals(3, result.getContent().size());

            verify(dishPersistencePort).findByRestaurantId(RESTAURANT_ID, null, pageable);
        }

        @Test
        @DisplayName("Validación: Debe filtrar platos por categoría")
        void shouldFilterDishesByCategory() {
            // Arrange
            DishModel dish1 = createDish(1L, "Postre 1", CategoryEnum.POSTRES);
            DishModel dish2 = createDish(2L, "Postre 2", CategoryEnum.POSTRES);

            List<DishModel> dishes = Arrays.asList(dish1, dish2);
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> dishPage = new PageImpl<>(dishes, pageable, dishes.size());

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(RESTAURANT_ID, CategoryEnum.POSTRES, pageable))
                .thenReturn(dishPage);

            // Act
            Page<DishModel> result = dishUseCase.listDishesByRestaurant(RESTAURANT_ID, CategoryEnum.POSTRES, pageable);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            result.getContent().forEach(dish ->
                assertEquals(CategoryEnum.POSTRES, dish.getCategory())
            );

            verify(dishPersistencePort).findByRestaurantId(RESTAURANT_ID, CategoryEnum.POSTRES, pageable);
        }

        @Test
        @DisplayName("Error: Debe rechazar listado de restaurante inexistente")
        void shouldRejectListingFromNonExistentRestaurant() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.listDishesByRestaurant(RESTAURANT_ID, null, pageable));

            verify(dishPersistencePort, never()).findByRestaurantId(anyLong(), any(), any());
        }

        @Test
        @DisplayName("Validación: Debe retornar página vacía cuando no hay platos")
        void shouldReturnEmptyPageWhenNoDishes() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(RESTAURANT_ID, null, pageable)).thenReturn(emptyPage);

            // Act
            Page<DishModel> result = dishUseCase.listDishesByRestaurant(RESTAURANT_ID, null, pageable);

            // Assert
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Actualización con precio muy alto debe funcionar")
        void shouldAllowVeryHighPrice() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(1000000); // 1 millón

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            // Act
            dishUseCase.updateDish(validDish.getId(), updateData);

            // Assert
            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getPrice() == 1000000));
        }

        @Test
        @DisplayName("Edge Case: Debe rechazar precio -1")
        void shouldRejectNegativePrice() {
            // Arrange
            DishModel updateData = new DishModel();
            updateData.setPrice(-1);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);

            // Act & Assert
            assertThrows(InvalidDishException.class, () -> dishUseCase.updateDish(validDish.getId(), updateData));
        }

        @Test
        @DisplayName("Edge Case: Toggle múltiple debe alternar correctamente")
        void shouldToggleMultipleTimes() {
            // Arrange
            validDish.setActive(true);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(RESTAURANT_ID)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(OWNER_ID);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenAnswer(invocation -> {
                DishModel dish = invocation.getArgument(0);
                validDish.setActive(dish.getActive());
                return dish;
            });

            // Act & Assert - Primera vez: activo -> inactivo
            dishUseCase.toggleDishStatus(validDish.getId());
            assertFalse(validDish.getActive());

            // Segunda vez: inactivo -> activo
            dishUseCase.toggleDishStatus(validDish.getId());
            assertTrue(validDish.getActive());
        }
    }

    // Métodos auxiliares
    private DishModel createDish(Long id, String name, CategoryEnum category) {
        DishModel dish = new DishModel();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(15000);
        dish.setDescription("Descripción de " + name);
        dish.setUrlImage("https://example.com/" + id + ".jpg");
        dish.setCategory(category);
        dish.setActive(true);
        dish.setRestaurantId(RESTAURANT_ID);
        return dish;
    }
}

