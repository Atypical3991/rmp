package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.PaymentStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class PaymentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PaymentStatusEnum status;

    private Long billItemId;
}
