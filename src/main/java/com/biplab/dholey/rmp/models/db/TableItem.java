package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class TableItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long tableNumber;
    private Long occupancy;
    @Enumerated(EnumType.STRING )
    private TableItemStatusEnum status;

}
