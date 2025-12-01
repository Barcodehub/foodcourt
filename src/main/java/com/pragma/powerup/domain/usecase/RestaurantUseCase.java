package com.pragma.powerup.domain.usecase;

import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.exception.InvalidRestaurantException;
import com.pragma.powerup.domain.exception.RestaurantAlreadyExistsException;
import com.pragma.powerup.domain.exception.UserNotFoundException;
import com.pragma.powerup.domain.exception.UserNotOwnerException;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IRestaurantPersistencePort;
import com.pragma.powerup.domain.spi.IUserValidationPort;
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
            throw new RestaurantAlreadyExistsException("Ya existe un restaurante con el NIT: " + restaurantModel.getNit());
        }

        Optional<UserResponseModel> user = userValidationPort.getUserById(restaurantModel.getOwnerId());
        if (user.isEmpty()) {
            throw new UserNotFoundException("No se encontrÃ³ el usuario con ID: " + restaurantModel.getOwnerId());
        }

        if (!userValidationPort.isUserOwner(restaurantModel.getOwnerId())) {
            throw new UserNotOwnerException("El usuario no tiene el rol de propietario");
        }

        return restaurantPersistencePort.saveRestaurant(restaurantModel);
    }

    @Override
    public Page<RestaurantModel> listRestaurants(Pageable pageable) {
        return restaurantPersistencePort.findAll(pageable);
    }

    private void validateRestaurantName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRestaurantException("El nombre del restaurante no puede estar vacÃ­o");
        }

        if (name.matches("^[0-9]+$")) {
            throw new InvalidRestaurantException("El nombre del restaurante no puede contener solo nÃºmeros");
        }
    }
}
