package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.domain.exception.DishNotFoundException;
import com.pragma.powerup.domain.exception.InvalidDishException;
import com.pragma.powerup.domain.exception.RestaurantNotFoundException;
import com.pragma.powerup.domain.exception.UnauthorizedDishOperationException;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.infrastructure.exceptionhandler.ExceptionResponse;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
            throw new RestaurantNotFoundException(ExceptionResponse.RESTAURANT_NOT_FOUND.getMessage());
        }

        validateRestaurantOwnership(restaurant.get());
        dishModel.setActive(true);
        return dishPersistencePort.saveDish(dishModel);
    }

    @Override
    public DishModel updateDish(Long dishId, DishModel dishModel) {
        DishModel dish = getValidatedDish(dishId);

        if (dishModel.getPrice() != null) {
            if (dishModel.getPrice() <= 0) {
                throw new InvalidDishException(ExceptionResponse.DISH_PRICE_INVALID.getMessage());
            }
            dish.setPrice(dishModel.getPrice());
        }

        if (dishModel.getDescription() != null && !dishModel.getDescription().trim().isEmpty()) {
            dish.setDescription(dishModel.getDescription());
        }

        return dishPersistencePort.updateDish(dish);
    }

    @Override
    public DishModel toggleDishStatus(Long id) {
        DishModel dish = getValidatedDish(id);
        dish.setActive(!dish.getActive());

        return dishPersistencePort.updateDish(dish);
    }

    @Override
    public Page<DishModel> listDishesByRestaurant(Long restaurantId, CategoryEnum category, Pageable pageable) {
        Optional<RestaurantModel> restaurant = restaurantPersistencePort.findById(restaurantId);
        if (restaurant.isEmpty()) {
            throw new RestaurantNotFoundException(ExceptionResponse.RESTAURANT_NOT_FOUND.getMessage());
        }

        return dishPersistencePort.findByRestaurantId(restaurantId, category, pageable);
    }

    private DishModel getValidatedDish(Long dishId) {
        Optional<DishModel> existingDish = dishPersistencePort.findById(dishId);
        if (existingDish.isEmpty()) {
            throw new DishNotFoundException(ExceptionResponse.DISH_NOT_FOUND.getMessage());
        }
        DishModel dish = existingDish.get();

        Optional<RestaurantModel> restaurant = restaurantPersistencePort.findById(dish.getRestaurantId());
        if (restaurant.isEmpty()) {
            throw new RestaurantNotFoundException(ExceptionResponse.DISH_RESTAURANT_NOT_FOUND.getMessage());
        }
        validateRestaurantOwnership(restaurant.get());

        return dish;
    }

    private void validateRestaurantOwnership(RestaurantModel restaurant) {
        Long currentUserId = securityContextPort.getCurrentUserId();

        if (!restaurant.getOwnerId().equals(currentUserId)) {
            throw new UnauthorizedDishOperationException(ExceptionResponse.DISH_UNAUTHORIZED_OWNER.getMessage() + " Restaurante pertenece al propietario con ID: " + restaurant.getOwnerId());
        }
    }
}
