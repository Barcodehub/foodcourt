package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.apifirst.api.RestaurantsApi;
import com.pragma.powerup.apifirst.model.RestaurantDataResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantListResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantRequestDto;
import com.pragma.powerup.application.handler.IRestaurantHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class RestaurantController implements RestaurantsApi {

    private final IRestaurantHandler restaurantHandler;

    @Override
    @RequireRole(RoleEnum.ADMINISTRADOR)
    public ResponseEntity<RestaurantDataResponseDto> createRestaurant(RestaurantRequestDto restaurantRequestDto) {
        RestaurantDataResponseDto response = restaurantHandler.createRestaurant(restaurantRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @RequireRole(RoleEnum.ADMINISTRADOR)
    public ResponseEntity<RestaurantListResponseDto> listRestaurants(Integer page, Integer size) {
        RestaurantListResponseDto response = restaurantHandler.listRestaurants(page, size);
        return ResponseEntity.ok(response);
    }
}
