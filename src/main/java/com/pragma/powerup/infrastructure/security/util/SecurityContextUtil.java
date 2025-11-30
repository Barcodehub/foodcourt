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
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "role"
     */
    public String getCurrentUserRole() {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                return null;
            }

            // Extraer el token sin el prefijo "Bearer "
            String jwtToken = token.substring(7);

            // Decodificar el payload (segunda parte del JWT)
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // Extraer el claim "role"
            return (String) claims.get("role");
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Decodifica el token JWT del header Authorization y extrae el claim "userId"
     */
    public Long getCurrentUserId() {
        try {
            String token = request.getHeader("Authorization");
            if (token == null || !token.startsWith("Bearer ")) {
                throw new RuntimeException("Usuario no autenticado");
            }

            // Extraer el token sin el prefijo "Bearer "
            String jwtToken = token.substring(7);

            // Decodificar el payload (segunda parte del JWT)
            String[] parts = jwtToken.split("\\.");
            if (parts.length < 2) {
                throw new RuntimeException("Token JWT inválido");
            }

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // Extraer el claim "userId"
            Object userIdObj = claims.get("userId");
            if (userIdObj instanceof Number) {
                return ((Number) userIdObj).longValue();
            }
            throw new RuntimeException("No se pudo obtener el ID del usuario autenticado");
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener el ID del usuario: " + e.getMessage());
        }
    }

    public boolean hasRole(String roleName) {
        try {
            String currentRole = getCurrentUserRole();
            return currentRole != null && currentRole.equalsIgnoreCase(roleName);
        } catch (Exception e) {
            return false;
        }
    }
}

