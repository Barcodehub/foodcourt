package com.pragma.powerup.infrastructure.out.jpa.adapter;

import com.pragma.powerup.domain.model.DishModel;
import com.pragma.powerup.domain.spi.IDishPersistencePort;
import com.pragma.powerup.infrastructure.out.jpa.entity.DishEntity;
import com.pragma.powerup.infrastructure.out.jpa.mapper.IDishEntityMapper;
import com.pragma.powerup.infrastructure.out.jpa.repository.IDishRepository;
import lombok.RequiredArgsConstructor;
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
}