package com.pragma.powerup.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEfficiencySummaryModel {
    private Integer totalEmployees;
    private Double restaurantAverageDurationMinutes;
    private Double bestEmployeeAverageDurationMinutes;
    private Double worstEmployeeAverageDurationMinutes;
    private Integer totalOrdersProcessed;
}

