package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.TableCleanRequestTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantTableService;
import com.biplab.dholey.rmp.services.TableNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TableCleaningServiceDaemon extends Thread {


    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final RestaurantTableService restaurantTableService;

    private final TableNotificationService tableNotificationService;

    @Autowired
    public TableCleaningServiceDaemon(RestaurantTableService restaurantTableService, TableNotificationService tableNotificationService) {
        this.restaurantTableService = restaurantTableService;
        this.tableNotificationService = tableNotificationService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TableCleanRequestTaskQueueModel tableCleanRequestTaskQueueModel = restaurantTableService.popCleaningTableTask();
                tableNotificationService.tableCleanedNotification(tableCleanRequestTaskQueueModel.getTableId());
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
