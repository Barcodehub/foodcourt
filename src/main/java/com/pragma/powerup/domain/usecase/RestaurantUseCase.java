package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.domain.exception.InvalidRestaurantException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotFoundException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exceptionhandler.ExceptionResponse;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@RequiredArgsConstructor
public class RestaurantUseCase implements IRestaurantServicePort {

    private final IRestaurantPersistencePort restaurantPersistencePort;
    private final IUserValidationPort userValidationPort;

    @Override
    public RestaurantModel createRestaurant(RestaurantModel restaurantModel) {
        validateRestaurantName(restaurantModel.getName());

        Optional<RestaurantModel> existingRestaurant = restaurantPersistencePort.findByNit(restaurantModel.getNit());
        if (existingRestaurant.isPresent()) {
            throw new RestaurantAlreadyExistsException(ExceptionResponse.RESTAURANT_ALREADY_EXISTS.getMessage());
        }

        Optional<UserResponseModel> user = userValidationPort.getUserById(restaurantModel.getOwnerId());
        if (user.isEmpty()) {
            throw new UserNotFoundException(ExceptionResponse.RESTAURANT_OWNER_NOT_FOUND.getMessage());
        }

        if (!userValidationPort.isUserOwner(restaurantModel.getOwnerId())) {
            throw new UserNotOwnerException(ExceptionResponse.RESTAURANT_OWNER_INVALID_ROLE.getMessage());
        }

        return restaurantPersistencePort.saveRestaurant(restaurantModel);
    }

    @Override
    public Page<RestaurantModel> listRestaurants(Pageable pageable) {
        return restaurantPersistencePort.findAll(pageable);
    }

    private void validateRestaurantName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRestaurantException(ExceptionResponse.RESTAURANT_NAME_EMPTY.getMessage());
        }

        if (name.matches("^[0-9]+$")) {
            throw new InvalidRestaurantException(ExceptionResponse.RESTAURANT_NAME_NUMERIC.getMessage());
        }
    }
}
