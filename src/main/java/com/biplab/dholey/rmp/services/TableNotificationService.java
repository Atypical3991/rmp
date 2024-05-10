package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.NotificationTaskQueueModel;
import com.biplab.dholey.rmp.repositories.TableBookRepository;
import com.biplab.dholey.rmp.util.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class TableNotificationService {

    private final int PLACE_ORDER_MAX_NOTIFICATION_COUNT = 3;

    private final int CONSECUTIVE_PLACE_ORDER_MAX_NOTIFICATION_ = 3;

    private final int GENERATE_BILL_MAX_NOTIFICATION_COUNT = 3;

    private final int CONSECUTIVE_GENERATE_BILL_MAX_NOTIFICATION_ = 3;

    private final int MAKE_PAYMENT_MAX_NOTIFICATION_COUNT = 3;

    private final int CONSECUTIVE_MAKE_PAYMENT_MAX_NOTIFICATION_ = 3;
    private final TaskQueue tableNotificationTaskQueue = new TaskQueue("restaurant_table_notification_task_queue", 1000);
    @Autowired
    TableBookRepository tableBookRepository;
    @Autowired
    private RestaurantTableService restaurantTableService;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public void tableCleanedNotification(Long tableBookItemId) {
        tableNotificationTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your table cleaning is done."));
    }

    public void paymentReceivedNotification(Long tableBookItemId) {
        tableNotificationTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your payment has been received."));
    }

    public void billGeneratedNotification(Long tableBookItemId) {
        tableNotificationTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Bill for your orders has been generated successfully."));
    }

    public void ordersPlacedNotification(Long tableBookItemId) {
        tableNotificationTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your orders has been placed successfully."));
    }

    public void ordersServedNotification(Long tableBookItemId) {
        tableNotificationTaskQueue.pushTask(new NotificationTaskQueueModel(tableBookItemId, "Your orders has been served successfully."));
    }

    public NotificationTaskQueueModel popNotificationTask() {
        return (NotificationTaskQueueModel) tableNotificationTaskQueue.popTask();
    }
}
