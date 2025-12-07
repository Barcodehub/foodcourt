package com.pragma.powerup.domain.model;

import com.pragma.powerup.domain.enums.OrderStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAuditModel {

    private Long orderId;
    private Long restaurantId;
    private Long clientId;
    private OrderStatusEnum previousStatus;
    private OrderStatusEnum newStatus;
    private Long changedByUserId;
    private String changedByRole;
    private String actionType;
    private Long employeeId;
    private String notes;
}

