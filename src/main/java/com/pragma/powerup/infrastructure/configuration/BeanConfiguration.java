package com.pragma.powerup.infrastructure.configuration;

import com.pragma.powerup.domain.api.IDishServicePort;
import com.pragma.powerup.domain.api.IOrderServicePort;
import com.pragma.powerup.domain.api.IRestaurantServicePort;
import com.pragma.powerup.domain.spi.*;
import com.pragma.powerup.domain.usecase.DishUseCase;
import com.pragma.powerup.domain.usecase.OrderUseCase;
import com.pragma.powerup.domain.usecase.RestaurantUseCase;
import com.pragma.powerup.domain.usecase.SmsUseCase;
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
    public SmsUseCase smsUseCase(ISmsNotificationPort smsNotificationPort) {
        return new SmsUseCase(smsNotificationPort);
    }

    @Bean
    public IOrderServicePort orderServicePort(
            IOrderPersistencePort orderPersistencePort,
            ISecurityContextPort securityContextPort,
            IUserValidationPort userValidationPort,
            IOrderAuditPort orderAuditPort,
            SmsUseCase smsUseCase) {
        return new OrderUseCase(
                orderPersistencePort,
                securityContextPort,
                userValidationPort,
                orderAuditPort,
                smsUseCase
        );
    }
}
