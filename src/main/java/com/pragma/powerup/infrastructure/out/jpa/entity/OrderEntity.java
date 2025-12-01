package com.pragma.powerup.infrastructure.out.jpa.entity;



import com.pragma.powerup.domain.enums.OrderStatusEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "restaurant_id", nullable = false)
    private RestaurantEntity restaurant;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderDishEntity> dishes;

    @Enumerated(EnumType.STRING)
    private OrderStatusEnum status;

    private Long client;
    private Long employee;

    @CreationTimestamp
    private LocalDateTime createdAt;

}



