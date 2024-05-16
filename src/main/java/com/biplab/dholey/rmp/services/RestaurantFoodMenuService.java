package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.FoodMenuControllerModifyMenuItem;
import com.biplab.dholey.rmp.models.api.request.FoodMenuItemControllerAddMenuItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.FoodMenuControllerFetchFoodMenuResponse;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.repositories.FoodMenuItemRepository;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RestaurantFoodMenuService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantFoodMenuService.class));
    @Autowired
    private RestaurantRecipeService restaurantRecipeService;
    @Autowired
    private FoodMenuItemRepository foodMenuItemRepository;

    public FoodMenuItem getFoodMenuItemById(Long foodMenuId) {
        logger.info("getFoodMenuItemById called", "getFoodMenuItemById", RestaurantFoodMenuService.class.toString(), Map.of("foodMenuId", foodMenuId.toString()));
        return foodMenuItemRepository.findById(foodMenuId).orElse(null);
    }

    public FoodMenuControllerFetchFoodMenuResponse fetchFoodMenuItem() {
        try {
            logger.info("fetchFoodMenuItem called!!", "fetchFoodMenuItem", RestaurantFoodMenuService.class.toString(), null);
            List<FoodMenuItem> foodMenuItems = foodMenuItemRepository.findAll();
            FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData data = new FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData();
            data.setMenuItems(new ArrayList<>());
            for (FoodMenuItem foodMenuItem : foodMenuItems) {
                FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData.MenuItem menuItem = new FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData.MenuItem();
                menuItem.setId(foodMenuItem.getId());
                menuItem.setName(foodMenuItem.getName());
                menuItem.setPrice(foodMenuItem.getPrice());
                menuItem.setDescription(foodMenuItem.getDescription());
                data.getMenuItems().add(menuItem);
            }
            logger.info("fetchFoodMenuItem processed successfully!!", "fetchFoodMenuItem", RestaurantFoodMenuService.class.toString(), null);
            return (FoodMenuControllerFetchFoodMenuResponse) new FoodMenuControllerFetchFoodMenuResponse().getSuccessResponse(data, "foodMenuItems fetched successfully!!");
        } catch (Exception e) {
            logger.error("Exception raised inside fetchFoodMenuItem!!", "fetchFoodMenuItem", RestaurantFoodMenuService.class.toString(), e, null);
            return (FoodMenuControllerFetchFoodMenuResponse) new FoodMenuControllerFetchFoodMenuResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse addFoodMenuItem(FoodMenuItemControllerAddMenuItemRequest addMenuItemRequest) {
        try {
            logger.info("addFoodMenuItem called!!", "addFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("addMenuItemRequest", addMenuItemRequest.toString()));
            Long recipeItemId = addMenuItemRequest.getRecipeItemId();
            FoodMenuItem foodMenuItem = foodMenuItemRepository.findByRecipeItemId(recipeItemId);
            if (foodMenuItem != null) {
                logger.info("foodMenuItem already present for this Recipe!!", "addFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("addMenuItemRequest", addMenuItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("foodMenuItem already present for this Recipe!!");
            }

            RecipeItem recipeItem = restaurantRecipeService.fetchRecipeItemById(recipeItemId);
            if (recipeItem == null) {
                logger.error("RecipeItem not found!!", "addFoodMenuItem", RestaurantFoodMenuService.class.toString(), new RuntimeException("RecipeItem not found!!"), Map.of("addMenuItemRequest", addMenuItemRequest.toString()));
                throw new RuntimeException("RecipeItem not found!!");
            }

            FoodMenuItem newFoodMenuItem = new FoodMenuItem();
            newFoodMenuItem.setRecipeItemId(recipeItemId);
            newFoodMenuItem.setPrice(addMenuItemRequest.getPrice());
            newFoodMenuItem.setName(recipeItem.getName());
            newFoodMenuItem.setDescription(recipeItem.getDescription());
            foodMenuItemRepository.save(newFoodMenuItem);
            logger.info("addFoodMenuItem  processed successfully!!", "addFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("addMenuItemRequest", addMenuItemRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Food menu item added successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in addFoodMenuItem!!", "addFoodMenuItem", RestaurantFoodMenuService.class.toString(), e, Map.of("addMenuItemRequest", addMenuItemRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse removeFoodMenuItem(Long foodMenuItemId) {
        try {
            logger.info("removeFoodMenuItem called!!", "removeFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("foodMenuItemId", foodMenuItemId.toString()));
            Optional<FoodMenuItem> foodMenuItemOpt = foodMenuItemRepository.findById(foodMenuItemId);
            if (foodMenuItemOpt.isEmpty()) {
                logger.info("foodMenuItem not found", "removeFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("foodMenuItemId", foodMenuItemId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("Empty foodMenuItemOpt received for foodMenuItemId: " + foodMenuItemId);
            }
            foodMenuItemRepository.deleteById(foodMenuItemId);
            logger.info("removeFoodMenuItem processed successfully!!", "removeFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("foodMenuItemId", foodMenuItemId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Removed food menu successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in removeFoodMenuItem!!", "removeFoodMenuItem", RestaurantFoodMenuService.class.toString(), e, Map.of("foodMenuItemId", foodMenuItemId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse modifyFoodMenuItem(FoodMenuControllerModifyMenuItem modifyFoodMenuItemRequest) {
        try {
            logger.info("modifyFoodMenuItem called!!", "modifyFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("modifyFoodMenuItemRequest", modifyFoodMenuItemRequest.toString()));
            Long foodMenuItemId = modifyFoodMenuItemRequest.getFoodMenuItemId();
            Optional<FoodMenuItem> foodMenuItemOpt = foodMenuItemRepository.findById(foodMenuItemId);
            if (foodMenuItemOpt.isEmpty()) {
                logger.info("foodMenuItem not found!!", "modifyFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("modifyFoodMenuItemRequest", modifyFoodMenuItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("Empty foodMenuItemOpt received for foodMenuItemId: " + foodMenuItemId);
            }
            FoodMenuItem foodMenuItem = foodMenuItemOpt.get();
            if (modifyFoodMenuItemRequest.getPrice() != null) {
                foodMenuItem.setPrice(modifyFoodMenuItemRequest.getPrice());
            }
            if (modifyFoodMenuItemRequest.getDescription() != null) {
                foodMenuItem.setDescription(modifyFoodMenuItemRequest.getDescription());
            }
            if (modifyFoodMenuItemRequest.getName() != null) {
                foodMenuItem.setName(modifyFoodMenuItemRequest.getName());
            }
            foodMenuItemRepository.save(foodMenuItem);
            logger.info("modifyFoodMenuItem processed successfully!!", "modifyFoodMenuItem", RestaurantFoodMenuService.class.toString(), Map.of("modifyFoodMenuItemRequest", modifyFoodMenuItemRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Food menu modified successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in modifyFoodMenuItem!!", "modifyFoodMenuItemRequest", RestaurantFoodMenuService.class.toString(), e, Map.of("modifyFoodMenuItemRequest", modifyFoodMenuItemRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }
}
