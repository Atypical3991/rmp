package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.models.util.TaskQueueModels.GenerateBillTaskQueueModel;
import com.biplab.dholey.rmp.services.RestaurantBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class BillGeneratorDaemon extends Thread {

    private static final int MAX_NUMBER_OF_WORKERS = 10;
    @Autowired
    private RestaurantBillService restaurantBillService;

    @Autowired
    public BillGeneratorDaemon(RestaurantBillService restaurantBillService) {
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
                executorService.submit(() -> restaurantBillService.processGenerateBillTask(generateBillTaskQueueModel));
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
