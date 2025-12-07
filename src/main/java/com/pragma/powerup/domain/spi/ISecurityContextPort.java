package com.pragma.powerup.domain.spi;

public interface ISecurityContextPort {

    Long getCurrentUserId();
    String getCurrentUserRole();
    boolean hasRole(String roleName);
    String getCurrentUserName();
}

