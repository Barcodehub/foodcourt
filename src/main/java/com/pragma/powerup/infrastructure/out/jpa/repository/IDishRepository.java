package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IDishRepository extends JpaRepository<DishEntity, Long> {
    Page<DishEntity> findByRestaurantIdAndActiveIsTrue(Long restaurantId, Pageable pageable);
    Page<DishEntity> findByRestaurantIdAndCategoryAndActiveIsTrue(Long restaurantId, CategoryEnum category, Pageable pageable);
}