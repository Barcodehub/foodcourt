package com.pragma.powerup.application.mapper;

import com.pragma.powerup.apifirst.model.*;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.RestaurantModel;
import com.pragma.powerup.domain.model.DishModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface IOrderMapper {

    @Mapping(target = "restaurant", expression = "java(mapRestaurantIdToModel(requestDto.getRestaurantId()))")
    OrderModel toModel(OrderRequestDto requestDto);

    @Mapping(target = "dish", expression = "java(mapDishIdToModel(dishRequest.getDishId()))")
    OrderDishModel toModel(OrderDishRequestDto dishRequest);

    @Mapping(target = "restaurantId", source = "restaurant.id")
    OrderResponseDto toResponseDto(OrderModel model);

    //@Mapping(target = "dishId", source = "dish.id")
    OrderDishResponseDto toResponseDto(OrderDishModel orderDishModel);

    default OrderListResponseDto toListResponseDto(Page<OrderModel> orderPage) {
        OrderListResponseDto responseDto = new OrderListResponseDto();

        List<OrderResponseDto> orderDtos = orderPage.getContent()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());

        responseDto.setData(orderDtos);
        responseDto.setPage(orderPage.getNumber());
        responseDto.setSize(orderPage.getSize());
        responseDto.setTotalElements(orderPage.getTotalElements());
        responseDto.setTotalPages(orderPage.getTotalPages());
        responseDto.setLast(orderPage.isLast());

        return responseDto;
    }

    default RestaurantModel mapRestaurantIdToModel(Long restaurantId) {
        if (restaurantId == null) {
            return null;
        }
        RestaurantModel restaurant = new RestaurantModel();
        restaurant.setId(restaurantId);
        return restaurant;
    }

    default DishModel mapDishIdToModel(Long dishId) {
        if (dishId == null) {
            return null;
        }
        DishModel dish = new DishModel();
        dish.setId(dishId);
        return dish;
    }
}