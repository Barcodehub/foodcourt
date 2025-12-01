package com.pragma.powerup.infrastructure.out.jpa.mapper;

import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.OrderDishModel;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderDishEntity;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {IRestaurantEntityMapper.class, IDishEntityMapper.class})
public interface IOrderEntityMapper {

    @Mapping(target = "dishes.order", ignore = true)
    OrderModel toDomain(OrderEntity entity);

    @Mapping(target = "dishes", source = "dishes")
    OrderEntity toEntity(OrderModel model);

    @Mapping(target = "order", ignore = true)
    OrderDishEntity toEntity(OrderDishModel model);

    @Mapping(target = "order", ignore = true)
    OrderDishModel toDomain(OrderDishEntity entity);

    @AfterMapping
    default void setOrderDishRelationship(@MappingTarget OrderEntity orderEntity) {
        if (orderEntity.getDishes() != null) {
            orderEntity.getDishes().forEach(dish -> dish.setOrder(orderEntity));
        }
    }
}