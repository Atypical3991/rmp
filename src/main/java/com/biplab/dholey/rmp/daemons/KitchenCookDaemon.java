package com.biplab.dholey.rmp.daemons;


import com.biplab.dholey.rmp.models.util.TaskQueueModels.PrepareFoodTaskQueueModel;
import com.biplab.dholey.rmp.services.KitchenCookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class KitchenCookDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    @Autowired
    private KitchenCookService kitchenCookService;

    @Autowired
    public KitchenCookDaemon(KitchenCookService kitchenCookService) {
        this.kitchenCookService = kitchenCookService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                PrepareFoodTaskQueueModel queuedOrder = kitchenCookService.fetchAllOrdersToBeProcessed();
                executorService.submit(() -> kitchenCookService.processOrder(queuedOrder.getOrderId()));
                Thread.sleep(1000); // Simulate some task
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status
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