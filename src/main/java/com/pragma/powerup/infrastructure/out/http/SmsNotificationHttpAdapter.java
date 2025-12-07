package com.pragma.powerup.infrastructure.out.http;

import com.pragma.powerup.domain.model.SmsNotificationModel;
import com.pragma.powerup.domain.spi.ISmsNotificationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsNotificationHttpAdapter implements ISmsNotificationPort {

    private final RestTemplate restTemplate;

    @Value("${sms.service.url:http://localhost:8082}")
    private String smsServiceUrl;

    @Override
    public void sendSms(SmsNotificationModel smsNotification) {
        try {
            String url = smsServiceUrl + "/sms/send";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("phoneNumber", smsNotification.getPhoneNumber());
            requestBody.put("message", smsNotification.getMessage());
            requestBody.put("metadata", smsNotification.getMetadata());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            restTemplate.postForEntity(url, request, Map.class);

            log.info("SMS enviado exitosamente al número: {}", smsNotification.getPhoneNumber());
        } catch (Exception e) {
            log.error("Error al enviar SMS al número {}: {}", smsNotification.getPhoneNumber(), e.getMessage());
            // No lanzamos excepción aqui
        }
    }
}

