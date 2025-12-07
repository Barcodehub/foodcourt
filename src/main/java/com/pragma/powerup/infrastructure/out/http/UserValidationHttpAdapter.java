package com.pragma.powerup.infrastructure.out.http;

import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exception.RemoteServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserValidationHttpAdapter implements IUserValidationPort {

    private final RestTemplate restTemplate;

    @Autowired
    private HttpServletRequest request;

    @Value("${users.service.url:http://localhost:8081}")
    private String usersServiceUrl;

    @Override
    public Optional<UserResponseModel> getUserById(Long userId) {
        try {
            String url = usersServiceUrl + "/users/" + userId;

            String token = request.getHeader("Authorization");
            HttpHeaders headers = new HttpHeaders();
            if (token != null) {
                headers.set("Authorization", token);
            }

            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("data")) {
                Map<String, Object> userData = (Map<String, Object>) response.get("data");

                Object roleObj = userData.get("role");
                String roleName = null;
                if (roleObj instanceof Map) {
                    Map<String, Object> roleMap = (Map<String, Object>) roleObj;
                    roleName = (String) roleMap.get("name");
                } else if (roleObj instanceof String) {
                    roleName = (String) roleObj;
                }

                UserResponseModel user = new UserResponseModel();
                user.setId(((Number) userData.get("id")).longValue());
                user.setName((String) userData.get("name"));
                user.setLastName((String) userData.get("lastName"));
                user.setEmail((String) userData.get("email"));
                user.setPhoneNumber((String) userData.get("phoneNumber"));
                user.setRole(roleName);

                if (userData.containsKey("restaurantWorkId") && userData.get("restaurantWorkId") != null) {
                    user.setRestaurantWorkId(((Number) userData.get("restaurantWorkId")).longValue());
                }

                return Optional.of(user);
            }

            return Optional.empty();
        } catch (HttpClientErrorException e) {
            // Manejo de errores específicos del cliente HTTP
            String errorMessage = "Error desconocido";
            try {
                String responseBody = e.getResponseBodyAsString();
                if (responseBody != null && !responseBody.isEmpty()) {
                    ObjectMapper mapper = new ObjectMapper();
                    Map<String, Object> errorMap = mapper.readValue(responseBody, Map.class);
                    if (errorMap.containsKey("message")) {
                        errorMessage = errorMap.get("message").toString();
                    } else if (errorMap.containsKey("error")) {
                        errorMessage = errorMap.get("error").toString();
                    }
                }
            } catch (IOException ex) {
                // mensaje
            }
            HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
            throw new RemoteServiceException(errorMessage, status);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public boolean isUserOwner(Long userId) {
        Optional<UserResponseModel> user = getUserById(userId);
        return user.map(u -> "PROPIETARIO".equalsIgnoreCase(u.getRole()) ||
                              "OWNER".equalsIgnoreCase(u.getRole()))
                   .orElse(false);
    }
}