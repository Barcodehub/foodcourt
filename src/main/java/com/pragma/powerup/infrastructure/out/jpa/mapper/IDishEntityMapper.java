package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.RestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IDishEntityMapper {

    @Mapping(target = "restaurantId", source = "restaurant.id")
    DishModel toDomain(DishEntity entity);

    @Mapping(target = "restaurant", expression = "java(mapRestaurantIdToEntity(model.getRestaurantId()))")
    DishEntity toEntity(DishModel model);

    default RestaurantEntity mapRestaurantIdToEntity(Long restaurantId) {
        if (restaurantId == null) {
            return null;
        }
        RestaurantEntity restaurant = new RestaurantEntity();
        restaurant.setId(restaurantId);
        return restaurant;
    }
}