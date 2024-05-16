package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.BookedTableStatusEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
public class TableBookedItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tableId;

    private List<Long> orderIds = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private BookedTableStatusEnum status;

    private LocalDateTime tableBookedAt;

    private LocalDateTime billGenerateAt;

    private LocalDateTime paymentReceivedAt;

    private LocalDateTime tableUnBookedAt;

    private LocalDateTime lastOrderPlacedAt;

    private LocalDateTime lastOrderServedAt;

    private String notification;

    private LocalDateTime placeOrderNotificationSentAt;

    private int placeOrderNotificationSentCount;

    private LocalDateTime generateBillNotificationSentAt;

    private int generateBillNotificationSentCount;

    private LocalDateTime makePaymentNotificationSentAt;

    private int makePaymentNotificationSentCount;

}
