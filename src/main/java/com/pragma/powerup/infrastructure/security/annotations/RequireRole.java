package com.pragma.powerup.infrastructure.security.annotations;

import com.pragma.powerup.domain.enums.RoleEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para verificar que el usuario tenga alguno de los roles especificados
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    RoleEnum[] value();
}

