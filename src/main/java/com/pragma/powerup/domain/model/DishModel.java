package com.pragma.powerup.domain.model;

import com.pragma.powerup.domain.enums.CategoryEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DishModel {
    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String urlImage;
    private CategoryEnum category;
    private Boolean active;
    private Long restaurantId;
}
