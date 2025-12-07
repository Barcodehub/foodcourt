package com.pragma.powerup.infrastructure.security.aspect;

import com.pragma.powerup.domain.exception.ForbiddenException;
import com.pragma.powerup.infrastructure.security.annotations.RequireRole;
import com.pragma.powerup.infrastructure.security.util.SecurityContextUtil;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleSecurityAspect {

    private static final Logger log = LoggerFactory.getLogger(RoleSecurityAspect.class);
    private final SecurityContextUtil securityContextUtil;

    @Before("@annotation(requireRole)")
    public void checkRole(RequireRole requireRole) {
        String currentRole = securityContextUtil.getCurrentUserRole();

        boolean hasRequiredRole = Arrays.stream(requireRole.value())
                .anyMatch(role -> role.getName().equalsIgnoreCase(currentRole));

        if (!hasRequiredRole) {
            throw new ForbiddenException(
                "No tiene permisos para realizar esta acción. Rol requerido: "
                + Arrays.toString(requireRole.value()));
        }
    }
}
