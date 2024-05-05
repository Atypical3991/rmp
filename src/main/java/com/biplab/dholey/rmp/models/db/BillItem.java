package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class BillItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    private List<Long> orderItemIds;

    private Long tableItemId;

    private Double payable;

    @Enumerated(EnumType.STRING)
    private BillItemStatusEnum status;
}
