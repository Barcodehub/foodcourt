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

@ExtendWith(MockitoExtension.class)
@DisplayName("DishUseCase - Gestion de Platos")
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
    private final Long ownerId = 1L;
    private final Long restaurantId = 1L;

    @BeforeEach
    void setUp() {
        restaurant = new RestaurantModel();
        restaurant.setId(restaurantId);
        restaurant.setName("Test Restaurant");
        restaurant.setOwnerId(ownerId);

        validDish = new DishModel();
        validDish.setId(1L);
        validDish.setName("Bandeja Paisa");
        validDish.setPrice(25000);
        validDish.setDescription("Plato tipico colombiano");
        validDish.setUrlImage("https://example.com/bandeja.jpg");
        validDish.setCategory(CategoryEnum.PLATOS_FUERTES);
        validDish.setActive(true);
        validDish.setRestaurantId(restaurantId);
    }

    @Nested
    @DisplayName("HU3: Crear Plato")
    class CreateDishTests {

        @Test
        @DisplayName("Happy Path: Debe crear plato con datos validos")
        void shouldCreateDishWithValidData() {
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            DishModel result = dishUseCase.createDish(validDish);

            assertNotNull(result);
            assertEquals("Bandeja Paisa", result.getName());
            assertEquals(25000, result.getPrice());
            assertTrue(result.getActive());

            verify(restaurantPersistencePort).findById(restaurantId);
            verify(securityContextPort).getCurrentUserId();
            verify(dishPersistencePort).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Validacion: Debe activar el plato automaticamente al crearlo")
        void shouldAutomaticallyActivateDishOnCreation() {
            validDish.setActive(null);

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenAnswer(invocation -> {
                DishModel dish = invocation.getArgument(0);
                assertTrue(dish.getActive(), "El plato debe estar activo");
                return dish;
            });

            dishUseCase.createDish(validDish);

            verify(dishPersistencePort).saveDish(argThat(DishModel::getActive));
        }

        @Test
        @DisplayName("Error: Debe rechazar creacion si restaurante no existe")
        void shouldRejectCreationWhenRestaurantNotExists() {
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.empty());

            assertThrows(RestaurantNotFoundException.class, () -> dishUseCase.createDish(validDish));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Error: Debe rechazar creacion por usuario no propietario")
        void shouldRejectCreationByNonOwner() {
            Long differentOwnerId = 999L;

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(differentOwnerId);

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
            DishModel updateData = new DishModel();
            updateData.setPrice(30000);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            DishModel result = dishUseCase.updateDish(validDish.getId(), updateData);

            assertNotNull(result);
            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getPrice() == 30000));
        }

        @Test
        @DisplayName("Happy Path: Debe actualizar descripcion del plato")
        void shouldUpdateDishDescription() {
            DishModel updateData = new DishModel();
            updateData.setDescription("Nueva descripcion actualizada");

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            dishUseCase.updateDish(validDish.getId(), updateData);

            verify(dishPersistencePort).saveDish(argThat(dish ->
                "Nueva descripcion actualizada".equals(dish.getDescription())
            ));
        }

        @Test
        @DisplayName("Validacion: Debe rechazar precio negativo o cero")
        void shouldRejectNegativeOrZeroPrice() {
            DishModel updateData = new DishModel();
            updateData.setPrice(0);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);

            Long dishId = validDish.getId();
            assertThrows(InvalidDishException.class, () -> {
                dishUseCase.updateDish(dishId, updateData);
            });

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }

        @Test
        @DisplayName("Error: Debe rechazar actualizacion de plato inexistente")
        void shouldRejectUpdateOfNonExistentDish() {
            DishModel updateData = new DishModel();
            updateData.setPrice(30000);
            Long dishId = validDish.getId();

            when(dishPersistencePort.findById(dishId)).thenReturn(Optional.empty());

            assertThrows(DishNotFoundException.class, () -> dishUseCase.updateDish(dishId, updateData));
        }

        @Test
        @DisplayName("Validacion: Debe ignorar descripcion vacia en actualizacion")
        void shouldIgnoreEmptyDescriptionInUpdate() {
            String originalDescription = validDish.getDescription();
            DishModel updateData = new DishModel();
            updateData.setDescription("");

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            dishUseCase.updateDish(validDish.getId(), updateData);

            verify(dishPersistencePort).saveDish(argThat(dish ->
                originalDescription.equals(dish.getDescription())
            ));
        }

        @Test
        @DisplayName("Validacion: Debe permitir actualizacion solo de precio")
        void shouldAllowPriceOnlyUpdate() {
            DishModel updateData = new DishModel();
            updateData.setPrice(35000);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            dishUseCase.updateDish(validDish.getId(), updateData);

            verify(dishPersistencePort).saveDish(any(DishModel.class));
        }
    }

    @Nested
    @DisplayName("HU7: Habilitar/Deshabilitar Plato")
    class ToggleDishStatusTests {

        @Test
        @DisplayName("Happy Path: Debe deshabilitar plato activo")
        void shouldDisableActiveDish() {
            validDish.setActive(true);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            Long dishId = validDish.getId();
            dishUseCase.toggleDishStatus(dishId);

            verify(dishPersistencePort).saveDish(argThat(dish -> !dish.getActive()));
        }

        @Test
        @DisplayName("Happy Path: Debe habilitar plato inactivo")
        void shouldEnableInactiveDish() {
            validDish.setActive(false);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            Long dishId = validDish.getId();
            dishUseCase.toggleDishStatus(dishId);

            verify(dishPersistencePort).saveDish(argThat(DishModel::getActive));
        }

        @Test
        @DisplayName("Error: Debe rechazar toggle por usuario no autorizado")
        void shouldRejectToggleByUnauthorizedUser() {
            Long differentOwnerId = 999L;
            Long dishId = validDish.getId();

            when(dishPersistencePort.findById(dishId)).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(differentOwnerId);

            assertThrows(UnauthorizedDishOperationException.class,
                () -> dishUseCase.toggleDishStatus(dishId));

            verify(dishPersistencePort, never()).saveDish(any(DishModel.class));
        }
    }

    @Nested
    @DisplayName("HU10: Listar los platos de un restaurante")
    class ListDishesTests {

        @Test
        @DisplayName("Happy Path: Debe listar platos de un restaurante")
        void shouldListDishesFromRestaurant() {
            DishModel dish1 = createDish(1L, "Plato 1", CategoryEnum.PLATOS_FUERTES);
            DishModel dish2 = createDish(2L, "Plato 2", CategoryEnum.SOPAS);
            DishModel dish3 = createDish(3L, "Plato 3", CategoryEnum.POSTRES);

            List<DishModel> dishes = Arrays.asList(dish1, dish2, dish3);
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> dishPage = new PageImpl<>(dishes, pageable, dishes.size());

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(restaurantId, null, pageable)).thenReturn(dishPage);

            Page<DishModel> result = dishUseCase.listDishesByRestaurant(restaurantId, null, pageable);

            assertNotNull(result);
            assertEquals(3, result.getTotalElements());
            assertEquals(3, result.getContent().size());

            verify(dishPersistencePort).findByRestaurantId(restaurantId, null, pageable);
        }

        @Test
        @DisplayName("Validacion: Debe filtrar platos por categoria")
        void shouldFilterDishesByCategory() {
            DishModel dish1 = createDish(1L, "Postre 1", CategoryEnum.POSTRES);
            DishModel dish2 = createDish(2L, "Postre 2", CategoryEnum.POSTRES);

            List<DishModel> dishes = Arrays.asList(dish1, dish2);
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> dishPage = new PageImpl<>(dishes, pageable, dishes.size());

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(restaurantId, CategoryEnum.POSTRES, pageable))
                .thenReturn(dishPage);

            Page<DishModel> result = dishUseCase.listDishesByRestaurant(restaurantId, CategoryEnum.POSTRES, pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            result.getContent().forEach(dish ->
                assertEquals(CategoryEnum.POSTRES, dish.getCategory())
            );

            verify(dishPersistencePort).findByRestaurantId(restaurantId, CategoryEnum.POSTRES, pageable);
        }

        @Test
        @DisplayName("Error: Debe rechazar listado de restaurante inexistente")
        void shouldRejectListingFromNonExistentRestaurant() {
            Pageable pageable = PageRequest.of(0, 10);

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.empty());

            assertThrows(RestaurantNotFoundException.class,
                () -> dishUseCase.listDishesByRestaurant(restaurantId, null, pageable));

            verify(dishPersistencePort, never()).findByRestaurantId(anyLong(), any(), any());
        }

        @Test
        @DisplayName("Validacion: Debe retornar pagina vacia cuando no hay platos")
        void shouldReturnEmptyPageWhenNoDishes() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<DishModel> emptyPage = new PageImpl<>(Arrays.asList(), pageable, 0);

            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(dishPersistencePort.findByRestaurantId(restaurantId, null, pageable)).thenReturn(emptyPage);

            Page<DishModel> result = dishUseCase.listDishesByRestaurant(restaurantId, null, pageable);

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Edge Case: Actualizacion con precio muy alto debe funcionar")
        void shouldAllowVeryHighPrice() {
            DishModel updateData = new DishModel();
            updateData.setPrice(1000000);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenReturn(validDish);

            dishUseCase.updateDish(validDish.getId(), updateData);

            verify(dishPersistencePort).saveDish(argThat(dish -> dish.getPrice() == 1000000));
        }

        @Test
        @DisplayName("Edge Case: Debe rechazar precio -1")
        void shouldRejectNegativePrice() {
            DishModel updateData = new DishModel();
            updateData.setPrice(-1);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);

            Long dishId = validDish.getId();
            assertThrows(InvalidDishException.class, () -> {
                dishUseCase.updateDish(dishId, updateData);
            });
        }

        @Test
        @DisplayName("Edge Case: Toggle multiple debe alternar correctamente")
        void shouldToggleMultipleTimes() {
            validDish.setActive(true);

            when(dishPersistencePort.findById(validDish.getId())).thenReturn(Optional.of(validDish));
            when(restaurantPersistencePort.findById(restaurantId)).thenReturn(Optional.of(restaurant));
            when(securityContextPort.getCurrentUserId()).thenReturn(ownerId);
            when(dishPersistencePort.saveDish(any(DishModel.class))).thenAnswer(invocation -> {
                DishModel dish = invocation.getArgument(0);
                validDish.setActive(dish.getActive());
                return dish;
            });

            dishUseCase.toggleDishStatus(validDish.getId());
            assertFalse(validDish.getActive());

            dishUseCase.toggleDishStatus(validDish.getId());
            assertTrue(validDish.getActive());
        }
    }

    private DishModel createDish(Long id, String name, CategoryEnum category) {
        DishModel dish = new DishModel();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(15000);
        dish.setDescription("Descripcion de " + name);
        dish.setUrlImage("https://example.com/" + id + ".jpg");
        dish.setCategory(category);
        dish.setActive(true);
        dish.setRestaurantId(restaurantId);
        return dish;
    }
}

