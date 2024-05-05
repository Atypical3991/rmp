package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.services.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TableCleaningServiceDaemon extends Thread {


    private static final int MAX_NUMBER_OF_CHEF = 10;
    private final RestaurantTableService restaurantTableService;

    @Autowired
    public TableCleaningServiceDaemon(RestaurantTableService restaurantTableService) {
        this.restaurantTableService = restaurantTableService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_CHEF);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                restaurantTableService.popCleaningTableTask();
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
