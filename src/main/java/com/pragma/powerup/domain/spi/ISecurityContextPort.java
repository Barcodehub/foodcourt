package com.pragma.powerup.domain.spi;

/**
 * Puerto SPI para acceder al contexto de seguridad
 */
public interface ISecurityContextPort {

    /**
     * Obtiene el ID del usuario autenticado actualmente
     * @return ID del usuario autenticado
     */
    Long getCurrentUserId();

    /**
     * Obtiene el rol del usuario autenticado actualmente
     * @return Nombre del rol del usuario autenticado
     */
    String getCurrentUserRole();

    /**
     * Verifica si el usuario autenticado tiene un rol específico
     * @param roleName Nombre del rol a verificar
     * @return true si el usuario tiene el rol, false en caso contrario
     */
    boolean hasRole(String roleName);
}

