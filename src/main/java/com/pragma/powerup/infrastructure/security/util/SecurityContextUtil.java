package com.pragma.powerup.infrastructure.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class SecurityContextUtil {

    private final HttpServletRequest request;
    private final ObjectMapper objectMapper;

    /**
     * Decodifica el token JWT del header Authorization y extrae el payload como Map
     */
    private Map<String, Object> getJwtClaims() {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return null;
            }
            String jwtToken = token.substring(7);
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            return objectMapper.readValue(payload, Map.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "role"
     */
    public String getCurrentUserRole() {
        Map<String, Object> claims = getJwtClaims();
        return claims != null ? (String) claims.get("role") : null;
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "userId"
     */
    public Long getCurrentUserId() {
        Map<String, Object> claims = getJwtClaims();
        if (claims == null) {
            throw new RuntimeException("Usuario no autenticado");
        }
        Object userIdObj = claims.get("userId");
        if (userIdObj instanceof Number) {
            return ((Number) userIdObj).longValue();
        }
        throw new RuntimeException("No se pudo obtener el ID del usuario autenticado");
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
        return claims != null ? (String) claims.get("name") : null;
    }
}
