package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UnauthorizedDishOperationException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DishUseCase implements IDishServicePort {

    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final ISecurityContextPort securityContextPort;

    @Override
    public DishModel createDish(DishModel dishModel) {
        Optional<RestaurantModel> restaurant = restaurantPersistencePort.findById(dishModel.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new RestaurantNotFoundException("No se encontrÃ³ el restaurante con ID: " + dishModel.getRestaurantId());
        }

        // Validar que el propietario autenticado sea dueño del restaurante
        validateRestaurantOwnership(restaurant.get());

        if (dishModel.getActive() == null) {
            dishModel.setActive(true);
        }

        return dishPersistencePort.saveDish(dishModel);
    }

    @Override
    public DishModel updateDish(Long dishId, DishModel dishModel) {
        Optional<DishModel> existingDish = dishPersistencePort.findById(dishId);
        if (existingDish.isEmpty()) {
            throw new DishNotFoundException("No se encontrÃ³ el plato con ID: " + dishId);
        }

        DishModel dish = existingDish.get();

        // Obtener el restaurante asociado al plato y validar propiedad
        Optional<RestaurantModel> restaurant = restaurantPersistencePort.findById(dish.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new RestaurantNotFoundException("No se encontró el restaurante asociado al plato");
        }
        validateRestaurantOwnership(restaurant.get());

        if (dishModel.getPrice() != null) {
            if (dishModel.getPrice() <= 0) {
                throw new InvalidDishException("El precio debe ser mayor a 0");
            }
            dish.setPrice(dishModel.getPrice());
        }

        if (dishModel.getDescription() != null && !dishModel.getDescription().trim().isEmpty()) {
            dish.setDescription(dishModel.getDescription());
        }

        return dishPersistencePort.updateDish(dish);
    }

    /**
     * Valida que el usuario autenticado sea el propietario del restaurante
     */
    private void validateRestaurantOwnership(RestaurantModel restaurant) {
        Long currentUserId = securityContextPort.getCurrentUserId();

        if (!restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedDishOperationException(
                "Solo el propietario del restaurante puede crear o modificar platos. " +
                "Restaurante pertenece al propietario con ID: " + restaurant.getOwnerId()
            );
        }
    }
}
