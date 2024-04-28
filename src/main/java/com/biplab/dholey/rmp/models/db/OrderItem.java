package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long foodMenuItemId;
    private Long quantity;
    private Long tableItemId;
    @Enumerated(EnumType.STRING)
    private OrderItemStatusEnum status;
}
