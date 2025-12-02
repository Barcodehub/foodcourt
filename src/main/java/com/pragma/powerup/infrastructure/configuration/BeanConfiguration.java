package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.*;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.OrderUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public IRestaurantServicePort restaurantServicePort(
            IRestaurantPersistencePort restaurantPersistencePort,
            IUserValidationPort userValidationPort) {
        return new RestaurantUseCase(restaurantPersistencePort, userValidationPort);
    }

    @Bean
    public IDishServicePort dishServicePort(
            IDishPersistencePort dishPersistencePort,
            IRestaurantPersistencePort restaurantPersistencePort,
            ISecurityContextPort securityContextPort) {
        return new DishUseCase(dishPersistencePort, restaurantPersistencePort, securityContextPort);
    }

    @Bean
    public IOrderServicePort orderServicePort(
            IOrderPersistencePort orderPersistencePort,
            IDishPersistencePort dishPersistencePort,
            IRestaurantPersistencePort restaurantPersistencePort,
            ISecurityContextPort securityContextPort,
            IUserValidationPort userValidationPort) {
        return new OrderUseCase(orderPersistencePort, dishPersistencePort, restaurantPersistencePort, securityContextPort, userValidationPort);
    }
}
