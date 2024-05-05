package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.models.util.PrepareFoodTaskQueueModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KitchenCookService {

    private static final int MAX_NUMBER_OF_CHEF = 10;
    @Autowired
    private RestaurantOrderService restaurantOrderService;
    @Autowired
    private FoodMenuService foodMenuService;
    @Autowired
    private RestaurantRecipeService restaurantRecipeService;

    @Transactional
    public void processOrder(Long orderId) {
        try {
            OrderItem orderItem = restaurantOrderService.fetchOrderById(orderId);
            if (orderItem.getStatus() == OrderItemStatusEnum.PICKED_BY_COOK) {

                Long foodMenuItemId = orderItem.getFoodMenuItemId();
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
                RecipeItem recipeItem = restaurantRecipeService.getRecipeItemById(foodMenuItem.getRecipeItemId());
                try {
                    Thread.sleep(recipeItem.getEstimatedTimeInMinutes() * 60 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.READY_TO_SERVE);
            } else if (orderItem.getStatus() == OrderItemStatusEnum.QUEUED) {
                restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.PICKED_BY_COOK);
                Long foodMenuItemId = orderItem.getFoodMenuItemId();
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
                RecipeItem recipeItem = restaurantRecipeService.getRecipeItemById(foodMenuItem.getRecipeItemId());
                try {
                    Thread.sleep(recipeItem.getEstimatedTimeInMinutes() * 60 * 1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.READY_TO_SERVE);
            } else {
                System.out.println("Unsupported order status!!");
            }
        } catch (Exception e) {

        }

    }

    public PrepareFoodTaskQueueModel fetchAllOrdersToBeProcessed() {
//        return restaurantOrderService.fetchAllInProgressAndQueuedOrders();
        return restaurantOrderService.popQueuedPrepareFoodTasks();
    }

}