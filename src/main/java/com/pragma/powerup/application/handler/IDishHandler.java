package com.pragma.powerup.application.handler;

import com.pragma.powerup.apifirst.model.DishDataResponseDto;
import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;

public interface IDishHandler {
    DishDataResponseDto createDish(DishRequestDto dishRequestDto);
    DishDataResponseDto updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto);
}
