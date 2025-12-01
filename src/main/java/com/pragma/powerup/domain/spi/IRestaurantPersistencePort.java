package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.RestaurantModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IRestaurantPersistencePort {
    RestaurantModel saveRestaurant(RestaurantModel restaurant);
    Optional<RestaurantModel> findById(Long id);
    Optional<RestaurantModel> findByNit(String nit);
    Page<RestaurantModel> findAll(Pageable pageable);
}
