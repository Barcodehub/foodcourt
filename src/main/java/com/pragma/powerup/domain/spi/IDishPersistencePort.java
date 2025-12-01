package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IDishPersistencePort {
    DishModel saveDish(DishModel dish);
    Optional<DishModel> findById(Long id);
    DishModel updateDish(DishModel dish);
    Page<DishModel> findByRestaurantId(Long restaurantId, CategoryEnum category, Pageable pageable);
}
