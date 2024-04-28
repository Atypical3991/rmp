package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class KitchenCookService {

    @Autowired
    private RestaurantOrderService restaurantOrderService;
    @Autowired
    private FoodMenuService foodMenuService;

    @Autowired
    private RestaurantRecipeService restaurantRecipeService;

    private static final  int MAX_NUMBER_OF_CHEF = 10;



    @Transactional
    public void processOrder(OrderItem orderItem){
        if(orderItem.getStatus() == OrderItemStatusEnum.PICKED_BY_COOK){
            Long foodMenuItemId = orderItem.getFoodMenuItemId();
            FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
            RecipeItem recipeItem = restaurantRecipeService.getRecipeItemById(foodMenuItem.getRecipeItemId());
            try{
                Thread.sleep(recipeItem.getEstimatedTimeInMinutes()*60*1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            restaurantOrderService.updateOrderStatus(orderItem.getId(),OrderItemStatusEnum.READY_TO_SERVE);
        }else if(orderItem.getStatus() == OrderItemStatusEnum.QUEUED){
            restaurantOrderService.updateOrderStatus(orderItem.getId(),OrderItemStatusEnum.PICKED_BY_COOK);
            Long foodMenuItemId = orderItem.getFoodMenuItemId();
            FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
            RecipeItem recipeItem = restaurantRecipeService.getRecipeItemById(foodMenuItem.getRecipeItemId());
            try{
                Thread.sleep(recipeItem.getEstimatedTimeInMinutes()*60*1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            restaurantOrderService.updateOrderStatus(orderItem.getId(),OrderItemStatusEnum.READY_TO_SERVE);
        }else{
            System.out.println("Unsupported order status!!");
        }
    }

    public List<OrderItem> fetchAllOrdersToBeProcessed(){
        return restaurantOrderService.fetchAllInProgressAndQueuedOrders();
    }

}