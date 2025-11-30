package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishResponseDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;
import com.pragma.powerup.domain.model.DishModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface IDishMapper {

    DishModel toModel(DishRequestDto requestDto);

    DishModel toModel(DishUpdateRequestDto updateRequestDto);

    DishResponseDto toResponseDto(DishModel model);

}