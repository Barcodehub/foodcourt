package com.pragma.powerup.infrastructure.input.rest;

import com.pragma.powerup.apifirst.api.DishesApi;
import com.pragma.powerup.apifirst.model.DishDataResponseDto;
import com.pragma.powerup.apifirst.model.DishListResponseDto;
import com.pragma.powerup.apifirst.model.DishRequestDto;
import com.pragma.powerup.apifirst.model.DishUpdateRequestDto;
import com.pragma.powerup.apifirst.model.ToggleDishResponseDto;
import com.pragma.powerup.application.handler.IDishHandler;
import com.pragma.powerup.domain.enums.RoleEnum;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DishController implements DishesApi {

    private final IDishHandler dishHandler;

    @Override
    @RequireRole(RoleEnum.PROPIETARIO)
    public ResponseEntity<DishDataResponseDto> createDish(DishRequestDto dishRequestDto) {
        DishDataResponseDto response = dishHandler.createDish(dishRequestDto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Override
    @RequireRole(RoleEnum.PROPIETARIO)
    public ResponseEntity<ToggleDishResponseDto> toggleDishStatus(Long id) {
        ToggleDishResponseDto response = dishHandler.toggleDishStatus(id);
        return ResponseEntity.ok(response);
    }

    @Override
    @RequireRole(RoleEnum.PROPIETARIO)
    public ResponseEntity<DishDataResponseDto> updateDish(Long id, DishUpdateRequestDto dishUpdateRequestDto) {
        DishDataResponseDto response = dishHandler.updateDish(id, dishUpdateRequestDto);
        return ResponseEntity.ok(response);
    }

    @Override
    @RequireRole(RoleEnum.CLIENTE)
    public ResponseEntity<DishListResponseDto> listDishesByRestaurant(Long restaurantId, Integer page, Integer size, String category) {
        DishListResponseDto response = dishHandler.listDishesByRestaurant(restaurantId, category, page, size);
        return ResponseEntity.ok(response);
    }
}
