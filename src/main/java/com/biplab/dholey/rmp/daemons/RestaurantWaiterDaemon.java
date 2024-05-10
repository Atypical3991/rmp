package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.services.RestaurantWaiterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class RestaurantWaiterDaemon extends Thread {


    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final RestaurantWaiterService restaurantWaiterService;

    @Autowired
    public RestaurantWaiterDaemon(RestaurantWaiterService restaurantWaiterService) {
        this.restaurantWaiterService = restaurantWaiterService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                restaurantWaiterService.serveReadyToServerFood();
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
