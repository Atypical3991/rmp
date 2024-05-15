package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.CartItemStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private List<Long> cartElementIds;
    private Double totalPrice;
    private Long tableId;
    @Enumerated(EnumType.STRING)
    private CartItemStatusEnum status;
}
