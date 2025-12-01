package com.pragma.powerup.application.handler.impl;

import com.pragma.powerup.apifirst.model.RestaurantDataResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantListResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantRequestDto;
import com.pragma.powerup.apifirst.model.RestaurantResponseDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.application.mapper.IRestaurantMapper;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.model.RestaurantModel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantHandler implements IRestaurantHandler {

    private final IRestaurantMapper restaurantMapper;
    private final IRestaurantServicePort restaurantServicePort;

    @Override
    public RestaurantDataResponseDto createRestaurant(RestaurantRequestDto restaurantRequestDto) {
        RestaurantModel restaurantModel = restaurantMapper.toModel(restaurantRequestDto);
        RestaurantModel savedRestaurant = restaurantServicePort.createRestaurant(restaurantModel);
        RestaurantResponseDto responseDto = restaurantMapper.toResponseDto(savedRestaurant);

        RestaurantDataResponseDto dataResponse = new RestaurantDataResponseDto();
        dataResponse.setData(responseDto);
        dataResponse.setMessage("Restaurante creado exitosamente");

        return dataResponse;
    }

    @Override
    public RestaurantListResponseDto listRestaurants(Integer page, Integer size) {
        // Crear Pageable con ordenamiento alfabético por nombre
        Pageable pageable = PageRequest.of(
            page != null ? page : 0,
            size != null ? size : 10,
            Sort.by("name").ascending()
        );

        Page<RestaurantModel> restaurantsPage = restaurantServicePort.listRestaurants(pageable);

        return restaurantMapper.toListResponseDto(restaurantsPage);
    }
}
