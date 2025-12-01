package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishResponseDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;
import com.pragma.powerup.apifirst.model.ToggleDishResponseDto;
import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IDishMapper {

    @Mapping(target = "category", expression = "java(mapDtoToModelCategory(requestDto.getCategory()))")
    DishModel toModel(DishRequestDto requestDto);

    DishModel toModel(DishUpdateRequestDto updateRequestDto);

    @Mapping(target = "category", expression = "java(mapModelToDtoCategory(model.getCategory()))")
    DishResponseDto toResponseDto(DishModel model);

    ToggleDishResponseDto toToggleResponseDto(DishModel toggledDish);

    // Conversión de DishRequestDto.CategoryEnum a domain.CategoryEnum
    default CategoryEnum mapDtoToModelCategory(DishRequestDto.CategoryEnum dtoCategory) {
        if (dtoCategory == null) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        return CategoryEnum.valueOf(dtoCategory.name());
    }

    // Conversión de domain.CategoryEnum a DishResponseDto.CategoryEnum
    default DishResponseDto.CategoryEnum mapModelToDtoCategory(CategoryEnum modelCategory) {
        if (modelCategory == null) {
            return null;
        }
        return DishResponseDto.CategoryEnum.valueOf(modelCategory.name());
    }
}