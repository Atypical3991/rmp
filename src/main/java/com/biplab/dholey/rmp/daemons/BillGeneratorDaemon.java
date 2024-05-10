package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.GenerateBillTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantBillService;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class BillGeneratorDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(BillGeneratorDaemon.class));
    @Autowired
    private RestaurantBillService restaurantBillService;

    @Autowired
    public BillGeneratorDaemon(RestaurantBillService restaurantBillService) {
        logger.info("BillGeneratorDaemon constructor called!!", "Constructor", BillGeneratorDaemon.class.toString(), null);
        this.restaurantBillService = restaurantBillService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                GenerateBillTaskQueueModel generateBillTaskQueueModel = restaurantBillService.popGenerateBillTask();
                if (generateBillTaskQueueModel != null) {
                    logger.info("BillGeneratorDaemon successfully received GenerateBillTaskQueueModel task.", "run", BillGeneratorDaemon.class.toString(), Map.of("task", generateBillTaskQueueModel.toString()));
                    executorService.submit(() -> restaurantBillService.processGenerateBillTask(generateBillTaskQueueModel));
                    Thread.sleep(1000);
                    logger.info("BillGeneratorDaemon successfully processed GenerateBillTaskQueueModel task.", "run", BillGeneratorDaemon.class.toString(), Map.of("task", generateBillTaskQueueModel.toString()));
                }
            } catch (InterruptedException e) {
                logger.error("BillGeneratorDaemon InterruptedException exception raised.", "run", BillGeneratorDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.error("BillGeneratorDaemon Generic exception raised.", "run", BillGeneratorDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.info("Shutting down executor service of BillGeneratorDaemon.", "run", BillGeneratorDaemon.class.toString(), null);
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Await termination of executor service of BillGeneratorDaemon interrupted.", "run", BillGeneratorDaemon.class.toString(), e, null);
        }
    }
}
