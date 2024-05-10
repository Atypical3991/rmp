package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.NotificationTaskQueueModel;
import com.biplab.dholey.rmp.util.CustomLogger;
import com.biplab.dholey.rmp.util.CustomTaskQueue;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class TableNotificationService {


    private final CustomTaskQueue tableNotificationCustomTaskQueue = new CustomTaskQueue("restaurant_table_notification_task_queue", 1000);

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(TableNotificationService.class));


    public void tableCleanedNotification(Long tableBookItemId) {
        logger.info("tableCleanedNotification called!!", "tableCleanedNotification", TableNotificationService.class.toString(), Map.of("tableBookItemId", tableBookItemId.toString()));
        try {
            tableNotificationCustomTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your table cleaning is done."));
        } catch (Exception e) {
            logger.error("Exception raised in tableCleanedNotification!!", "tableCleanedNotification", TableNotificationService.class.toString(), e, Map.of("tableBookItemId", tableBookItemId.toString()));
        }
    }

    public void paymentReceivedNotification(Long tableBookItemId) {
        logger.info("paymentReceivedNotification called!!", "paymentReceivedNotification", TableNotificationService.class.toString(), Map.of("tableBookItemId", tableBookItemId.toString()));
        try {
            tableNotificationCustomTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your payment has been received."));
        } catch (Exception e) {
            logger.error("Exception raised in paymentReceivedNotification!!", "paymentReceivedNotification", TableNotificationService.class.toString(), e, Map.of("tableBookItemId", tableBookItemId.toString()));
        }
    }

    public void billGeneratedNotification(Long tableBookItemId) {
        logger.info("billGeneratedNotification called!!", "billGeneratedNotification", TableNotificationService.class.toString(), Map.of("tableBookItemId", tableBookItemId.toString()));
        try {
            tableNotificationCustomTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Bill for your orders has been generated successfully."));
        } catch (Exception e) {
            logger.error("Exception raised in billGeneratedNotification!!", "billGeneratedNotification", TableNotificationService.class.toString(), e, Map.of("tableBookItemId", tableBookItemId.toString()));
        }
    }

    public void ordersPlacedNotification(Long tableBookItemId) {
        logger.info("ordersPlacedNotification called!!", "ordersPlacedNotification", TableNotificationService.class.toString(), Map.of("tableBookItemId", tableBookItemId.toString()));
        try {
            tableNotificationCustomTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your orders has been placed successfully."));
        } catch (Exception e) {
            logger.error("Exception raised in ordersPlacedNotification!!", "ordersPlacedNotification", TableNotificationService.class.toString(), e, Map.of("tableBookItemId", tableBookItemId.toString()));
        }
    }

    public void ordersServedNotification(Long tableBookItemId) {
        logger.info("ordersServedNotification called!!", "ordersServedNotification", TableNotificationService.class.toString(), Map.of("tableBookItemId", tableBookItemId.toString()));
        try {
            tableNotificationCustomTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your orders has been served successfully."));
        } catch (Exception e) {
            logger.error("Exception raised in ordersServedNotification!!", "ordersServedNotification", TableNotificationService.class.toString(), e, Map.of("tableBookItemId", tableBookItemId.toString()));
        }
    }

    public NotificationTaskQueueModel popNotificationTask() {
        logger.info("popNotificationTask called!!", "popNotificationTask", TableNotificationService.class.toString(), null);
        try {
            return (NotificationTaskQueueModel) tableNotificationCustomTaskQueue.popTask();
        } catch (Exception e) {
            logger.error("popNotificationTask called!!", "popNotificationTask", TableNotificationService.class.toString(), e, null);
            return null;
        }
    }
}
