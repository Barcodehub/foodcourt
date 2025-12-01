package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.model.DishModel;

public interface IDishServicePort {
    DishModel createDish(DishModel dishModel);
    DishModel updateDish(Long dishId, DishModel dishModel);

    DishModel toggleDishStatus(Long id);
}
