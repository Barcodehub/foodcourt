package com.pragma.powerup.domain.model.metrics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrdersDurationMetricsModel {
    private List<OrderDurationMetricModel> orders;
    private DurationSummaryModel summary;
}

