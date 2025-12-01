package com.pragma.powerup.application.mapper;

import com.pragma.powerup.domain.enums.CategoryEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class CategoryConverter {

    public CategoryEnum toEnum(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("La categoría es obligatoria");
        }
        try {
            return CategoryEnum.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Categoría no válida: " + category + ". Valores permitidos: " +
                getAllCategoryNames());
        }
    }

    public String toString(CategoryEnum category) {
        return category != null ? category.name() : null;
    }

    private String getAllCategoryNames() {
        return Arrays.stream(CategoryEnum.values())
            .map(CategoryEnum::name)
            .collect(Collectors.joining(", "));
    }
}

