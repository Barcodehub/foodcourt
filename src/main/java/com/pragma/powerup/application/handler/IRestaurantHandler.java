package com.pragma.powerup.application.handler;

import com.pragma.powerup.apifirst.model.RestaurantDataResponseDto;
import com.pragma.powerup.apifirst.model.RestaurantRequestDto;

public interface IRestaurantHandler {
    RestaurantDataResponseDto createRestaurant(RestaurantRequestDto restaurantRequestDto);
}
