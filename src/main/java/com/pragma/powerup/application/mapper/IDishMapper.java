package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.DishListResponseDto;
import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishResponseDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;
import com.pragma.powerup.apifirst.model.ToggleDishResponseDto;
import com.pragma.powerup.domain.model.DishModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = CategoryConverter.class)
public interface IDishMapper {

    @Mapping(target = "category", source = "category")
    DishModel toModel(DishRequestDto requestDto);

    DishModel toModel(DishUpdateRequestDto updateRequestDto);

    @Mapping(target = "category", source = "category")
    DishResponseDto toResponseDto(DishModel model);

    ToggleDishResponseDto toToggleResponseDto(DishModel toggledDish);


    default DishListResponseDto toListResponseDto(Page<DishModel> page) {
        List<DishResponseDto> content = page.getContent().stream()
            .map(this::toResponseDto)
            .toList();

        DishListResponseDto response = new DishListResponseDto();
        response.data(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}