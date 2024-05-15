package com.biplab.dholey.rmp.daemons;


import com.biplab.dholey.rmp.models.util.TaskQueueModels.PrepareFoodTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantKitchenCookService;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class KitchenCookDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(KitchenCookDaemon.class));
    @Autowired
    private RestaurantKitchenCookService restaurantKitchenCookService;


    @Autowired
    public KitchenCookDaemon(RestaurantKitchenCookService restaurantKitchenCookService) {
        logger.info("KitchenCookDaemon constructor called!!", "Constructor", KitchenCookDaemon.class.toString(), null);
        this.restaurantKitchenCookService = restaurantKitchenCookService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                PrepareFoodTaskQueueModel queuedOrder = restaurantKitchenCookService.fetchAllOrdersToBeProcessed();
                if (queuedOrder != null) {
                    logger.info("KitchenCookDaemon successfully received PrepareFoodTaskQueueModel task", "run", KitchenCookDaemon.class.toString(), Map.of("task", queuedOrder.toString()));
                    executorService.submit(() -> restaurantKitchenCookService.processOrder(queuedOrder.getOrderId()));
                    Thread.sleep(1000);
                    logger.info("KitchenCookDaemon successfully processed PrepareFoodTaskQueueModel task", "run", KitchenCookDaemon.class.toString(), Map.of("task", queuedOrder.toString()));
                }
            } catch (InterruptedException e) {
                logger.error("KitchenCookDaemon InterruptedException exception raised.", "run", KitchenCookDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("KitchenCookDaemon Generic exception raised.", "run", KitchenCookDaemon.class.toString(), e, null);
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.info("Shutting down KitchenCookDaemon's executor service.", "run", KitchenCookDaemon.class.toString(), null);
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Await termination of KitchenCookDaemon's executor service interrupted.", "run", KitchenCookDaemon.class.toString(), e, null);
        }
    }

}