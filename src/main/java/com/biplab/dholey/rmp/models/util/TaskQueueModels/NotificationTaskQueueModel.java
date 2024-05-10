package com.biplab.dholey.rmp.models.util.TaskQueueModels;

public class NotificationTaskQueueModel implements TaskQueueInterface {
    Long tableBookItemId;
    String notificationText;


    public NotificationTaskQueueModel(Long tableBookItemId, String notificationText) {
        this.tableBookItemId = tableBookItemId;
        this.notificationText = notificationText;
    }
}
