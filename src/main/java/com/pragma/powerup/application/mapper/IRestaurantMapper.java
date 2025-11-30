package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.RestaurantRequestDto;
import com.pragma.powerup.apifirst.model.RestaurantResponseDto;
import com.pragma.powerup.domain.model.RestaurantModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.net.URI;

@Mapper(componentModel = "spring")
public interface IRestaurantMapper {

    RestaurantModel toModel(RestaurantRequestDto requestDto);

    RestaurantResponseDto toResponseDto(RestaurantModel model);

}