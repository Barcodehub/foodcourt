package com.pragma.powerup.domain.usecase;


import com.pragma.powerup.domain.model.OrderModel;
import com.pragma.powerup.domain.model.SmsNotificationModel;
import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.*;
import lombok.RequiredArgsConstructor;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class SmsUseCase  {

    private static final String DIGITS = "0123456789";
    private static final int PIN_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final ISmsNotificationPort smsNotificationPort;


    protected void sendSmsNotification(UserResponseModel client, String message, OrderModel order) {
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", order.getId().toString());
        metadata.put("restaurantName", order.getRestaurant().getName());

        SmsNotificationModel smsNotification = new SmsNotificationModel(
                client.getPhoneNumber(),
                message,
                metadata
        );

        smsNotificationPort.sendSms(smsNotification);
    }

    protected void sendOrderReadyNotification(UserResponseModel client, OrderModel order) {
        String message = String.format(
                "Hola %s, tu pedido está listo para ser recogido en %s. Tu PIN de seguridad es: %s",
                client.getName(),
                order.getRestaurant().getName(),
                order.getSecurityPin()
        );

        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", order.getId().toString());
        metadata.put("restaurantName", order.getRestaurant().getName());
        metadata.put("securityPin", order.getSecurityPin());

        SmsNotificationModel smsNotification = new SmsNotificationModel(
                client.getPhoneNumber(),
                message,
                metadata
        );

        smsNotificationPort.sendSms(smsNotification);
    }

    protected void sendOrderCancelledNotification(UserResponseModel client, OrderModel order) {
        String message = String.format(
                "Hola %s, tu pedido en %s ha sido cancelado exitosamente.",
                client.getName(),
                order.getRestaurant().getName()
        );

        sendSmsNotification(client, message, order);
    }

    protected String generateSecurityPin() {
        StringBuilder pin = new StringBuilder(PIN_LENGTH);
        for (int i = 0; i < PIN_LENGTH; i++) {
            pin.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        }
        return pin.toString();
    }


}


