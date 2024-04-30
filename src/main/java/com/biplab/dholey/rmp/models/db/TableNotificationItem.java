package com.biplab.dholey.rmp.models.db;

import com.biplab.dholey.rmp.models.db.enums.NotificationTypeEnum;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class TableNotificationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tableId;

    @Enumerated(EnumType.STRING)
    private NotificationTypeEnum notificationType;

    private String notification;

    private LocalDateTime createdAt;
}
