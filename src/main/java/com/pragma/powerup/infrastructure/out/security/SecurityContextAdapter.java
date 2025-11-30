package com.pragma.powerup.infrastructure.out.security;

import com.pragma.powerup.domain.spi.ISecurityContextPort;
import com.pragma.powerup.infrastructure.security.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adaptador para acceder al contexto de seguridad desde el dominio
 */
@Component
@RequiredArgsConstructor
public class SecurityContextAdapter implements ISecurityContextPort {

    private final SecurityContextUtil securityContextUtil;

    @Override
    public Long getCurrentUserId() {
        return securityContextUtil.getCurrentUserId();
    }

    @Override
    public String getCurrentUserRole() {
        return securityContextUtil.getCurrentUserRole();
    }

    @Override
    public boolean hasRole(String roleName) {
        return securityContextUtil.hasRole(roleName);
    }
}

