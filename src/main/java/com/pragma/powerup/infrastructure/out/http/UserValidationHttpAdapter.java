package com.pragma.powerup.infrastructure.out.http;

import com.pragma.powerup.domain.model.UserResponseModel;
import com.pragma.powerup.domain.spi.IUserValidationPort;
import com.pragma.powerup.infrastructure.exception.RemoteServiceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import org.springframework.core.ParameterizedTypeReference;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserValidationHttpAdapter implements IUserValidationPort {

    private final RestTemplate restTemplate;
    private final HttpServletRequest request;

    @Value("${users.service.url:http://localhost:8081}")
    private String usersServiceUrl;

    private static final String RESTAURANT_WORK_ID = "restaurantWorkId";
    private static final String UNKNOWN_ERROR = "Error desconocido";

    @Override
    public Optional<UserResponseModel> getUserById(Long userId) {
        try {
            Map<String, Object> response = fetchUserFromRemoteService(userId);
            return extractUserFromResponse(response);
        } catch (HttpClientErrorException e) {
            throw handleHttpClientError(e);
        }
    }

    private Map<String, Object> fetchUserFromRemoteService(Long userId) {
        try {
            String url = usersServiceUrl + "/users/" + userId;
            HttpEntity<Void> entity = createHttpEntity();

            ParameterizedTypeReference<Map<String, Object>> typeRef =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    url, HttpMethod.GET, entity, typeRef);

            return responseEntity.getBody();
        } catch (Exception e) {
            if (e instanceof HttpClientErrorException httpClientError) {
                throw httpClientError;
            }
            throw new RemoteServiceException("Error al conectar con el servicio de usuarios", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpEntity<Void> createHttpEntity() {
        String token = request.getHeader("Authorization");
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("Authorization", token);
        }
        return new HttpEntity<>(headers);
    }

    private Optional<UserResponseModel> extractUserFromResponse(Map<String, Object> response) {
        if (response == null || !response.containsKey("data")) {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> userData = (Map<String, Object>) response.get("data");
        String roleName = extractRoleName(userData.get("role"));

        UserResponseModel user = buildUserModel(userData, roleName);
        return Optional.of(user);
    }

    private String extractRoleName(Object roleObj) {
        if (roleObj instanceof Map<?, ?> roleMap) {
            return (String) roleMap.get("name");
        } else if (roleObj instanceof String roleName) {
            return roleName;
        }
        return null;
    }

    private UserResponseModel buildUserModel(Map<String, Object> userData, String roleName) {
        UserResponseModel user = new UserResponseModel();
        user.setId(((Number) userData.get("id")).longValue());
        user.setName((String) userData.get("name"));
        user.setLastName((String) userData.get("lastName"));
        user.setEmail((String) userData.get("email"));
        user.setPhoneNumber((String) userData.get("phoneNumber"));
        user.setRole(roleName);

        if (userData.containsKey(RESTAURANT_WORK_ID) && userData.get(RESTAURANT_WORK_ID) != null) {
            user.setRestaurantWorkId(((Number) userData.get(RESTAURANT_WORK_ID)).longValue());
        }

        return user;
    }

    private RemoteServiceException handleHttpClientError(HttpClientErrorException e) {
        String errorMessage = extractErrorMessage(e);
        HttpStatus status = resolveHttpStatus(e);
        return new RemoteServiceException(errorMessage, status);
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        String responseBody = e.getResponseBodyAsString();
        if (responseBody.isEmpty()) {
            return UNKNOWN_ERROR;
        }

        return parseErrorMessage(responseBody);
    }

    private String parseErrorMessage(String responseBody) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ParameterizedTypeReference<Map<String, Object>> typeRef =
                    new ParameterizedTypeReference<>() {};
            Map<String, Object> errorMap = mapper.readValue(responseBody,
                    mapper.getTypeFactory().constructType(typeRef.getType()));

            return getErrorMessageFromMap(errorMap);
        } catch (IOException ex) {
            return UNKNOWN_ERROR;
        }
    }

    private String getErrorMessageFromMap(Map<String, Object> errorMap) {
        if (errorMap.containsKey("message")) {
            return errorMap.get("message").toString();
        } else if (errorMap.containsKey("error")) {
            return errorMap.get("error").toString();
        }
        return UNKNOWN_ERROR;
    }

    private HttpStatus resolveHttpStatus(HttpClientErrorException e) {
        HttpStatus status = HttpStatus.resolve(e.getStatusCode().value());
        return status != null ? status : HttpStatus.INTERNAL_SERVER_ERROR;
    }

    @Override
    public boolean isUserOwner(Long userId) {
        Optional<UserResponseModel> user = getUserById(userId);
        return user.map(u -> "PROPIETARIO".equalsIgnoreCase(u.getRole()) ||
                        "OWNER".equalsIgnoreCase(u.getRole()))
                .orElse(false);
    }
}