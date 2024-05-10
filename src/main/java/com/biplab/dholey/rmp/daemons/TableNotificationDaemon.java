package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.NotificationTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantTableService;
import com.biplab.dholey.rmp.services.TableNotificationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TableNotificationDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final TableNotificationService tableNotificationService;

    @Autowired
    public TableNotificationDaemon(TableNotificationService tableNotificationService) {
        this.tableNotificationService = tableNotificationService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                NotificationTaskQueueModel notificationTaskQueueModel = tableNotificationService.popNotificationTask();
                //TODO: Send notification functionality for customers needs to be implemented.
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
