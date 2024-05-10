package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.NotificationTaskQueueModel;
import com.biplab.dholey.rmp.services.TableNotificationService;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TableNotificationDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final TableNotificationService tableNotificationService;

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(TableNotificationDaemon.class));


    @Autowired
    public TableNotificationDaemon(TableNotificationService tableNotificationService) {
        logger.info("TableNotificationDaemon constructor called!!", "Constructor", TableNotificationDaemon.class.toString(), null);
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
                if (notificationTaskQueueModel != null) {
                    logger.info("TableNotificationDaemon successfully received task.", "run", TableNotificationDaemon.class.toString(), Map.of("task", notificationTaskQueueModel.toString()));
                    //TODO: Send notification functionality for customers needs to be implemented.
                    Thread.sleep(1000);
                    logger.info("TableNotificationDaemon successfully processed task.", "run", TableNotificationDaemon.class.toString(), Map.of("task", notificationTaskQueueModel.toString()));
                }

            } catch (InterruptedException e) {
                logger.error("TableNotificationDaemon InterruptedException exception raised.", "run", TableNotificationDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("TableNotificationDaemon Generic exception raised.", "run", TableNotificationDaemon.class.toString(), e, null);
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Await termination of TableNotificationDaemon's executor service interrupted.", "run", TableNotificationDaemon.class.toString(), e, null);
        }
    }
}
