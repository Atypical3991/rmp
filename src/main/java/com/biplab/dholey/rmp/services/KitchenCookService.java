package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.PrepareFoodTaskQueueModel;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class KitchenCookService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(KitchenCookService.class));
    @Autowired
    private RestaurantOrderService restaurantOrderService;
    @Autowired
    private FoodMenuService foodMenuService;
    @Autowired
    private RestaurantRecipeService restaurantRecipeService;

    @Transactional
    public void processOrder(Long orderId) {
        try {
            logger.info("processOrder called!!", "processOrder", KitchenCookService.class.toString(), null);
            OrderItem orderItem = restaurantOrderService.fetchOrderById(orderId);
            if (orderItem == null) {
                throw new RuntimeException("orderItem not found.");
            }
            if (orderItem.getStatus() == OrderItemStatusEnum.PICKED_BY_COOK) {
                Long foodMenuItemId = orderItem.getFoodMenuItemId();
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
                if (foodMenuItem == null) {
                    throw new RuntimeException("foodMenuItem not found");
                }
                RecipeItem recipeItem = restaurantRecipeService.fetchRecipeItemById(foodMenuItem.getRecipeItemId());
                try {
                    Thread.sleep(recipeItem.getEstimatedTimeInMinutes() * 60 * 1000);
                } catch (Exception e) {
                    logger.error("Exception raised inside PICKED_BY_COOK block!!", "processOrder", KitchenCookService.class.toString(), e, Map.of("orderItem", orderItem.toString()));
                }
                if (!restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.READY_TO_SERVE)) {
                    throw new RuntimeException("updateOrderStatus failed!!");
                }
                logger.info("processOrder called successfully resolved!!", "processOrder", KitchenCookService.class.toString(), null);
            } else if (orderItem.getStatus() == OrderItemStatusEnum.QUEUED) {
                if (restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.PICKED_BY_COOK)) {
                    throw new RuntimeException("updateOrderStatus failed!!");
                }
                Long foodMenuItemId = orderItem.getFoodMenuItemId();
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(foodMenuItemId);
                if (foodMenuItem == null) {
                    throw new RuntimeException("foodMenuItem not found");
                }
                RecipeItem recipeItem = restaurantRecipeService.fetchRecipeItemById(foodMenuItem.getRecipeItemId());
                try {
                    Thread.sleep(recipeItem.getEstimatedTimeInMinutes() * 60 * 1000);
                } catch (Exception e) {
                    logger.error("Exception raised inside QUEUED block!!", "processOrder", KitchenCookService.class.toString(), e, null);
                }
                if (restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.PICKED_BY_COOK)) {
                    throw new RuntimeException("updateOrderStatus failed!!");
                }
                logger.info("processOrder called successfully resolved!!", "processOrder", KitchenCookService.class.toString(), null);
            } else {
                logger.error("Un-supported OrderItem status received!!", "processOrder", KitchenCookService.class.toString(), new UnsupportedOperationException("Un-Supported status received inside."), Map.of("orderItem", orderItem.toString()));
            }
        } catch (Exception e) {
            logger.error("Something went wrong!!", "processOrder", KitchenCookService.class.toString(), e, null);
        }

    }

    public PrepareFoodTaskQueueModel fetchAllOrdersToBeProcessed() {
        logger.info("fetchAllOrdersToBeProcessed called!!", "fetchAllOrdersToBeProcessed", KitchenCookService.class.toString(), null);
        return restaurantOrderService.popQueuedPrepareFoodTasks();
    }

}