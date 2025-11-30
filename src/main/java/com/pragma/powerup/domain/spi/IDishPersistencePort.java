package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.DishModel;

import java.util.Optional;

public interface IDishPersistencePort {
    DishModel saveDish(DishModel dish);
    Optional<DishModel> findById(Long id);
    DishModel updateDish(DishModel dish);
}
