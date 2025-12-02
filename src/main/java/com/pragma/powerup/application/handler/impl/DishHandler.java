package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.*;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DishHandler implements IDishHandler {

    private final IDishMapper dishMapper;
    private final IDishServicePort dishServicePort;

    @Override
    public DishDataResponseDto createDish(DishRequestDto dishRequestDto) {
        DishModel dishModel = dishMapper.toModel(dishRequestDto);
        DishModel savedDish = dishServicePort.createDish(dishModel);
        DishResponseDto responseDto = dishMapper.toResponseDto(savedDish);

        DishDataResponseDto dataResponse = new DishDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Plato creado exitosamente");

        return dataResponse;
    }

    @Override
    public DishDataResponseDto updateDish(Long dishId, DishUpdateRequestDto dishUpdateRequestDto) {
        DishModel dishModel = dishMapper.toModel(dishUpdateRequestDto);
        DishModel updatedDish = dishServicePort.updateDish(dishId, dishModel);
        DishResponseDto responseDto = dishMapper.toResponseDto(updatedDish);

        DishDataResponseDto dataResponse = new DishDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Plato actualizado exitosamente");

        return dataResponse;
    }

    @Override
    public ToggleDishResponseDto toggleDishStatus(Long id) {
        DishModel toggledDish = dishServicePort.toggleDishStatus(id);
        ToggleDishResponseDto responseDto = dishMapper.toToggleResponseDto(toggledDish);

        ToggleDishResponseDto dataResponse = new ToggleDishResponseDto();
        dataResponse.setId(responseDto.getId());
        dataResponse.setActive(responseDto.getActive());

        return dataResponse;
    }

    @Override
    public DishListResponseDto listDishesByRestaurant(Long restaurantId, String category, Integer page, Integer size) {
        CategoryEnum categoryEnum = null;
        if (category != null && !category.isEmpty()) {
            categoryEnum = CategoryEnum.valueOf(category);
        }

        // Crear Pageable
        Pageable pageable = PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 10
        );

        Page<DishModel> dishesPage = dishServicePort.listDishesByRestaurant(restaurantId, categoryEnum, pageable);

        return dishMapper.toListResponseDto(dishesPage);
    }
}
