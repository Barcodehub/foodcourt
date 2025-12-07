package com.pragma.powerup.infrastructure.security.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

import com.pragma.powerup.infrastructure.exception.InvalidUserIdException;
import com.pragma.powerup.infrastructure.exception.UnauthenticatedUserException;

@Component
@RequiredArgsConstructor
public class SecurityContextUtil {

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    private Map<String, Object> getJwtClaims() {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return Map.of();
            }
            String jwtToken = token.substring(7);
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return Map.of();
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(payload, new TypeReference<>() {});
        } catch (Exception e) {
            return Map.of();
        }
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "role"
     */
    public String getCurrentUserRole() {
        Map<String, Object> claims = getJwtClaims();
        return claims.get("role") != null ? (String) claims.get("role") : null;
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "userId"
     */
    public Long getCurrentUserId() {
        Map<String, Object> claims = getJwtClaims();
        if (claims.isEmpty()) {
            throw new UnauthenticatedUserException("Usuario no autenticado");
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number number) {
            return number.longValue();
        }
        throw new InvalidUserIdException("No se pudo obtener el ID del usuario autenticado");
    }

    public boolean hasRole(String roleName) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equalsIgnoreCase(roleName);
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "name"
     */
    public String getCurrentUserName() {
        Map<String, Object> claims = getJwtClaims();
        return claims.get("name") != null ? (String) claims.get("name") : null;
    }
}
