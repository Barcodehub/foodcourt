package com.pragma.powerup.domain.model.metrics;
import java.time.OffsetDateTime;

import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.AllArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class OrderDurationMetricModel {
    private Long durationMinutes;
    private String finalStatus;
    private OffsetDateTime completedAt;
    private OffsetDateTime startedAt;
    private Long employeeId;
    private Long clientId;
    private Long orderId;
}




