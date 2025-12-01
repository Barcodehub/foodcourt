package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.enums.CategoryEnum;
import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DishJpaAdapter implements IDishPersistencePort {

    private final IDishRepository dishRepository;
    private final IDishEntityMapper dishEntityMapper;

    @Override
    public DishModel saveDish(DishModel dish) {
        DishEntity entity = dishRepository.save(dishEntityMapper.toEntity(dish));
        return dishEntityMapper.toDomain(entity);
    }

    @Override
    public Optional<DishModel> findById(Long id) {
        return dishRepository.findById(id)
                .map(dishEntityMapper::toDomain);
    }

    @Override
    public DishModel updateDish(DishModel dish) {
        DishEntity entity = dishRepository.save(dishEntityMapper.toEntity(dish));
        return dishEntityMapper.toDomain(entity);
    }

    @Override
    public Page<DishModel> findByRestaurantId(Long restaurantId, CategoryEnum category, Pageable pageable) {
        Page<DishEntity> entities;
        if (category != null) {
            entities = dishRepository.findByRestaurantIdAndCategory(restaurantId, category, pageable);
        } else {
            entities = dishRepository.findByRestaurantId(restaurantId, pageable);
        }
        return entities.map(dishEntityMapper::toDomain);
    }
}