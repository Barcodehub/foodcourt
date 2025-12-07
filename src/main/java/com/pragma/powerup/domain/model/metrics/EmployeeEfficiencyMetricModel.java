package com.pragma.powerup.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeEfficiencyMetricModel {
    private Integer rank;
    private Long employeeId;
    private Integer totalOrdersCompleted;
    private Integer totalOrdersDelivered;
    private Integer totalOrdersCancelled;
    private Double averageDurationMinutes;
    private Long minDurationMinutes;
    private Long maxDurationMinutes;
    private Double medianDurationMinutes;
}

