package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.TableCleanRequestTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantTableBookService;
import com.biplab.dholey.rmp.services.RestaurantTableService;
import com.biplab.dholey.rmp.services.TableNotificationService;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TableCleaningServiceDaemon extends Thread {


    private static final int MAX_NUMBER_OF_WORKERS = 10;

    private final RestaurantTableService restaurantTableService;

    private final TableNotificationService tableNotificationService;

    private final RestaurantTableBookService restaurantTableBookService;

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(TableCleaningServiceDaemon.class));


    @Autowired
    public TableCleaningServiceDaemon(RestaurantTableService restaurantTableService, TableNotificationService tableNotificationService, RestaurantTableBookService restaurantTableBookService) {
        logger.info("TableCleaningServiceDaemon constructor called!!", "Constructor", TableCleaningServiceDaemon.class.toString(), null);
        this.restaurantTableService = restaurantTableService;
        this.tableNotificationService = tableNotificationService;
        this.restaurantTableBookService = restaurantTableBookService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TableCleanRequestTaskQueueModel tableCleanRequestTaskQueueModel = restaurantTableService.popCleaningTableTask();
                if (tableCleanRequestTaskQueueModel != null) {
                    logger.info("TableCleaningServiceDaemon successfully received task.", "run", TableCleaningServiceDaemon.class.toString(), Map.of("task", tableCleanRequestTaskQueueModel.toString()));
                    TableItem tableItem = restaurantTableService.fetchTableById(tableCleanRequestTaskQueueModel.getTableId());
                    if (tableItem == null) {
                        throw new RuntimeException("TableItem not found.");
                    }
                    tableNotificationService.tableCleanedNotification(tableCleanRequestTaskQueueModel.getTableId());
                    Thread.sleep(1000);
                    restaurantTableBookService.updateTableCleanSuccessStatus(tableItem.getId());
                    logger.info("TableCleaningServiceDaemon successfully processed task.", "run", TableCleaningServiceDaemon.class.toString(), Map.of("task", tableCleanRequestTaskQueueModel.toString()));

                }
            } catch (InterruptedException e) {
                logger.error("TableCleaningServiceDaemon InterruptedException exception raised.", "run", TableCleaningServiceDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("TableCleaningServiceDaemon Generic exception raised.", "run", TableCleaningServiceDaemon.class.toString(), e, null);
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Await termination of TableCleaningServiceDaemon's executor service interrupted.", "run", TableCleaningServiceDaemon.class.toString(), e, null);
        }
    }
}
