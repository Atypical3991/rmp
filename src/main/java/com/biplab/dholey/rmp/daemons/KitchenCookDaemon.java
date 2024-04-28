package com.biplab.dholey.rmp.daemons;


import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.services.FoodMenuService;
import com.biplab.dholey.rmp.services.KitchenCookService;
import com.biplab.dholey.rmp.services.RestaurantOrderService;
import com.biplab.dholey.rmp.services.RestaurantRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class KitchenCookDaemon extends  Thread{

    private final KitchenCookService kitchenCookService;

    private static final  int MAX_NUMBER_OF_CHEF = 10;

    @Autowired
    public KitchenCookDaemon(KitchenCookService kitchenCookService){
        this.kitchenCookService = kitchenCookService;
        setDaemon(true);
        start();
    }

    @Override
    public void run() {
        ExecutorService executorService =  Executors.newFixedThreadPool(MAX_NUMBER_OF_CHEF);
        while (!Thread.currentThread().isInterrupted()) {
            try {
                List<OrderItem> orderItemsInProgressAndQueued  = kitchenCookService.fetchAllOrdersToBeProcessed();
                for(OrderItem orderItem: orderItemsInProgressAndQueued){
                    executorService.submit(() -> kitchenCookService.processOrder(orderItem));
                }
//                System.out.println("Daemon thread is running...");
                Thread.sleep(1000); // Simulate some task
            } catch (InterruptedException e) {
                // Handle interruption
                Thread.currentThread().interrupt(); // Restore interrupted status
            }
        }
        System.out.println("Daemon thread is stopped.");
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}