package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.*;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.application.mapper.IDishMapper;
import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.model.DishModel;
import lombok.RequiredArgsConstructor;
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
}
