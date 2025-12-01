package com.pragma.powerup.application.handler;

import com.pragma.powerup.apifirst.model.DishDataResponseDto;
import com.pragma.powerup.apifirst.model.DishListResponseDto;
import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;
import com.pragma.powerup.apifirst.model.ToggleDishResponseDto;

public interface IDishHandler {
    DishDataResponseDto createDish(DishRequestDto dishRequestDto);
    DishDataResponseDto updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto);
    ToggleDishResponseDto toggleDishStatus(Long id);
    DishListResponseDto listDishesByRestaurant(Long restaurantId, String category, Integer page, Integer size);
}
