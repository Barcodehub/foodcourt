package com.pragma.powerup.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseModel {
    private Long id;
    private String name;
    private String lastName;
    private String email;
    private String role;
    private Long restaurantWorkId;
}
