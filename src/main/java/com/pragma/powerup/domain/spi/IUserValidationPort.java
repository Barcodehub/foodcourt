package com.pragma.powerup.domain.spi;

import com.pragma.powerup.domain.model.UserResponseModel;

import java.util.Optional;

public interface IUserValidationPort {
    Optional<UserResponseModel> getUserById(Long userId);
    boolean isUserOwner(Long userId);
}
