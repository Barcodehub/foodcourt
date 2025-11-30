package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class DishUseCase implements IDishServicePort {

    private final IDishPersistencePort dishPersistencePort;
    private final IRestaurantPersistencePort restaurantPersistencePort;

    @Override
    public DishModel createDish(DishModel dishModel) {
        Optional<RestaurantModel> restaurant = restaurantPersistencePort.findById(dishModel.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new RestaurantNotFoundException("No se encontrÃ³ el restaurante con ID: " + dishModel.getRestaurantId());
        }

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
}
