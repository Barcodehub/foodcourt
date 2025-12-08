package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.OrderStatusEnum;
import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.spi.IOrderPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.OrderEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IOrderEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class OrderJpaAdapter implements IOrderPersistencePort {

    private final IOrderRepository orderRepository;
    private final IOrderEntityMapper orderEntityMapper;

    @Override
    public OrderModel saveOrder(OrderModel order) {
        OrderEntity entity = orderEntityMapper.toEntity(order);
        OrderEntity savedEntity = orderRepository.save(entity);
        return orderEntityMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<OrderModel> getActiveOrderByUserId(Long userId) {
        return orderRepository.findByClientAndStatusNotIn(
                userId,
                Arrays.asList(OrderStatusEnum.DELIVERED, OrderStatusEnum.CANCELLED)
        ).map(orderEntityMapper::toDomain);
    }

    @Override
    public Page<OrderModel> listOrdersByStatusAndRestaurant(OrderStatusEnum status, Long restaurantId, Pageable pageable) {
        Page<OrderEntity> entities = orderRepository.findByStatusAndRestaurantId(status, restaurantId, pageable);
        return entities.map(orderEntityMapper::toDomain);
    }

    @Override
    public Page<OrderModel> listOrdersByRestaurant(Long restaurantId, Pageable pageable) {
        Page<OrderEntity> entities = orderRepository.findByRestaurantId(restaurantId, pageable);
        return entities.map(orderEntityMapper::toDomain);
    }

    @Override
    public Optional<OrderModel> findById(Long orderId) {
        return orderRepository.findById(orderId)
                .map(orderEntityMapper::toDomain);
    }

    @Override
    public OrderModel updateOrder(OrderModel order) {
        OrderEntity entity = orderEntityMapper.toEntity(order);
        OrderEntity updatedEntity = orderRepository.save(entity);
        return orderEntityMapper.toDomain(updatedEntity);
    }

}