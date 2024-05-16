package com.biplab.dholey.rmp.daemons;

import com.biplab.dholey.rmp.services.RestaurantWaiterService;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;


@Service
public class RestaurantWaiterDaemon extends Thread {


    private static final int MAX_NUMBER_OF_WORKERS = 10;
    private final RestaurantWaiterService restaurantWaiterService;

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantWaiterDaemon.class));


    @Autowired
    public RestaurantWaiterDaemon(RestaurantWaiterService restaurantWaiterService) {
        logger.info("RestaurantWaiterDaemon constructor called!!", "Constructor", RestaurantWaiterDaemon.class.toString(), null);
        this.restaurantWaiterService = restaurantWaiterService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_NUMBER_OF_WORKERS);
        while (!Thread.currentThread().isInterrupted()) {
            try {
//                logger.info("RestaurantWaiterDaemon calling serveReadyToServerFood of RestaurantWaiterService.", "run", RestaurantWaiterDaemon.class.toString(), null);
                restaurantWaiterService.serveReadyToServerFood();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                logger.error("RestaurantWaiterDaemon InterruptedException exception raised.", "run", RestaurantWaiterDaemon.class.toString(), e, null);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.info("RestaurantWaiterDaemon Generic exception raised.", "run", RestaurantWaiterDaemon.class.toString(), null);
            }
        }
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Await termination of RestaurantWaiterDaemon's executor service interrupted.", "run", RestaurantWaiterDaemon.class.toString(), e, null);
        }
    }

}
