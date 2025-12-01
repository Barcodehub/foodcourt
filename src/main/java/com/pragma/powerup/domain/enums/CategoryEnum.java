package com.pragma.powerup.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CategoryEnum {
    ENTRADAS("Entradas"),
    PLATOS_FUERTES("Platos Fuertes"),
    SOPAS("Sopas"),
    ENSALADAS("Ensaladas"),
    BEBIDAS("Bebidas"),
    POSTRES("Postres"),
    ACOMPAÑAMIENTOS("Acompañamientos"),
    COMIDA_RAPIDA("Comida Rápida");

    private final String displayName;

    public static CategoryEnum fromString(String categoryName) {
        for (CategoryEnum category : CategoryEnum.values()) {
            if (category.name().equalsIgnoreCase(categoryName) ||
                category.displayName.equalsIgnoreCase(categoryName)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Categoría no válida: " + categoryName);
    }
}

