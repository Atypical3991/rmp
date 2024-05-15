package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long foodMenuItemId;

    private Long quantity;

    private Double totalPrice;

    private Long tableItemId;

    private boolean billGenerated = false;

    private LocalDateTime orderPlacedAt;

    private LocalDateTime orderServedAt;

    @Enumerated(EnumType.STRING)
    private OrderItemStatusEnum status;
}
