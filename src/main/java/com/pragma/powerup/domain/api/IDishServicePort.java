package com.pragma.powerup.domain.api;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IDishServicePort {
    DishModel createDish(DishModel dishModel);
    DishModel updateDish(Long dishId, DishModel dishModel);
    DishModel toggleDishStatus(Long id);
    Page<DishModel> listDishesByRestaurant(Long restaurantId, CategoryEnum category, Pageable pageable);
}
