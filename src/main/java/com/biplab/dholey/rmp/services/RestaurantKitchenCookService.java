package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.TaskQueueInterface;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantKitchenCookService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantKitchenCookService.class));
    @Autowired
    private RestaurantOrderService restaurantOrderService;
    @Autowired
    private RestaurantFoodMenuService restaurantFoodMenuService;
    @Autowired
    private RestaurantRecipeService restaurantRecipeService;

    @Transactional
    public void processOrder(Long orderId) {
        try {
            logger.info("processOrder called!!", "processOrder", RestaurantKitchenCookService.class.toString(), null);
            OrderItem orderItem = restaurantOrderService.fetchOrderById(orderId);
            if (orderItem == null) {
                throw new RuntimeException("orderItem not found.");
            }
            if (!restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.PICKED_BY_COOK)) {
                throw new RuntimeException("updateOrderStatus failed!!");
            }
            Long foodMenuItemId = orderItem.getFoodMenuItemId();
            FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(foodMenuItemId);
            if (foodMenuItem == null) {
                throw new RuntimeException("foodMenuItem not found");
            }
            RecipeItem recipeItem = restaurantRecipeService.fetchRecipeItemById(foodMenuItem.getRecipeItemId());
            try {
                Thread.sleep(recipeItem.getEstimatedTimeInMinutes() * 60 * 1000);
            } catch (Exception e) {
                logger.error("Exception raised inside QUEUED block!!", "processOrder", RestaurantKitchenCookService.class.toString(), e, null);
            }
            if (!restaurantOrderService.updateOrderStatus(orderItem.getId(), OrderItemStatusEnum.READY_TO_SERVE)) {
                throw new RuntimeException("updateOrderStatus failed!!");
            }
            logger.info("processOrder called successfully resolved!!", "processOrder", RestaurantKitchenCookService.class.toString(), null);
        } catch (Exception e) {
            logger.error("Something went wrong!!", "processOrder", RestaurantKitchenCookService.class.toString(), e, null);
        }

    }

    public TaskQueueInterface fetchAllOrdersToBeProcessed() {
        logger.debug("fetchAllOrdersToBeProcessed called!!", "fetchAllOrdersToBeProcessed", RestaurantKitchenCookService.class.toString(), null);
        return restaurantOrderService.popQueuedPrepareFoodTasks();
    }

}