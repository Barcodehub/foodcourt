package com.pragma.powerup.infrastructure.out.jpa.repository;

import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import com.pragma.powerup.domain.enums.OrderStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface IOrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByClientAndStatusNotIn(Long userId, java.util.List<OrderStatusEnum> excludedStatuses);
    Page<OrderEntity> findByStatusAndRestaurantId(OrderStatusEnum status, Long restaurantId, Pageable pageable);
    Page<OrderEntity> findByRestaurantId(Long restaurantId, Pageable pageable);
}