package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.StockItemOrderStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class StockItemOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String quantityMetric;

    private String quantity;

    @Enumerated(EnumType.STRING)
    private StockItemOrderStatusEnum status;
}
