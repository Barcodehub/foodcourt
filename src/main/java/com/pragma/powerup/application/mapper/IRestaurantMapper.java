package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.RestaurantLiteResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantListResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantRequestDto;
import com.pragma.powerup.apifirst.model.RestaurantResponseDto;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IRestaurantMapper {

    RestaurantModel toModel(RestaurantRequestDto requestDto);

    RestaurantResponseDto toResponseDto(RestaurantModel model);

    RestaurantLiteResponseDto toLiteResponseDto(RestaurantModel model);

    default RestaurantListResponseDto toListResponseDto(Page<RestaurantModel> page) {
        List<RestaurantLiteResponseDto> content = page.getContent().stream()
            .map(this::toLiteResponseDto)
            .collect(Collectors.toList());

        RestaurantListResponseDto response = new RestaurantListResponseDto();
        response.setContent(content);
        response.setPage(page.getNumber());
        response.setSize(page.getSize());
        response.setTotalElements(page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        response.setLast(page.isLast());

        return response;
    }
}