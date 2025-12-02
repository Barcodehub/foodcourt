package com.pragma.powerup.domain.spi;

/**
 * Puerto SPI para acceder al contexto de seguridad
 */
public interface ISecurityContextPort {

    Long getCurrentUserId();
    String getCurrentUserRole();
    boolean hasRole(String roleName);
    String getCurrentUserName();
}

