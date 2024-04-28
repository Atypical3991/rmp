package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.FoodMenuControllerModifyMenuItem;
import com.biplab.dholey.rmp.models.api.request.FoodMenuItemControllerAddMenuItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.FoodMenuControllerFetchFoodMenuResponse;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.repositories.FoodMenuItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class FoodMenuService {

    @Autowired
    private RestaurantRecipeService restaurantRecipeService;

    @Autowired
    private FoodMenuItemRepository foodMenuItemRepository;


    public FoodMenuItem getFoodMenuItemById(Long foodMenuId) {
        return foodMenuItemRepository.findById(foodMenuId).orElse(null);
    }

    public FoodMenuControllerFetchFoodMenuResponse fetchFoodMenuItem() {
        FoodMenuControllerFetchFoodMenuResponse parentResponse = new FoodMenuControllerFetchFoodMenuResponse();
        try {
            List<FoodMenuItem> foodMenuItems = foodMenuItemRepository.findAll();
            parentResponse.setData(new FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData());
            FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData response = parentResponse.getData();
            response.setMenuItems(new ArrayList<>());
            for (FoodMenuItem foodMenuItem : foodMenuItems) {
                FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData.MenuItem menuItem = new FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData.MenuItem();
                RecipeItem recipeItem = restaurantRecipeService.getRecipeItemById(foodMenuItem.getRecipeItemId());
                menuItem.setName(recipeItem.getName());
                menuItem.setPrice(foodMenuItem.getPrice());
                menuItem.setDescription(recipeItem.getDescription());
                response.getMenuItems().add(menuItem);
            }
            parentResponse.setStatusCode(HttpStatus.OK.value());
            parentResponse.setMessage("foodMenuItems fetched successfully!!");
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse addFoodMenuItem(FoodMenuItemControllerAddMenuItemRequest addMenuItemRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            Long recipeItemId = addMenuItemRequest.getRecipeItemId();
            FoodMenuItem foodMenuItem = foodMenuItemRepository.findByRecipeItemId(recipeItemId);
            if (foodMenuItem != null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("FoodMenuItem not found for recipeItemId: "+recipeItemId);
                return parentResponse;
            }
            FoodMenuItem newFoodMenuItem = new FoodMenuItem();
            newFoodMenuItem.setRecipeItemId(recipeItemId);
            newFoodMenuItem.setPrice(addMenuItemRequest.getPrice());
            foodMenuItemRepository.save(newFoodMenuItem);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            BaseDBOperationsResponse.BaseDBOperationsResponseResponseData response = parentResponse.getData();
            response.setSuccess(true);
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return  parentResponse;
        }
    }

    public BaseDBOperationsResponse removeFoodMenuItem(Long foodMenuItemId) {
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try {
            Optional<FoodMenuItem> foodMenuItemOpt = foodMenuItemRepository.findById(foodMenuItemId);
            if (foodMenuItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty foodMenuItemOpt received for foodMenuItemId: "+foodMenuItemId);
                return  parentResponse;
            }
            foodMenuItemRepository.deleteById(foodMenuItemId);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            BaseDBOperationsResponse.BaseDBOperationsResponseResponseData response = parentResponse.getData();
            response.setSuccess(true);
            return  parentResponse;

        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return  parentResponse;
        }
    }

    public BaseDBOperationsResponse modifyFoodMenuItem(FoodMenuControllerModifyMenuItem modifyFoodMenuItemRequest) {
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try {
            Long foodMenuItemId = modifyFoodMenuItemRequest.getFoodMenuItemId();
            Optional<FoodMenuItem> foodMenuItemOpt = foodMenuItemRepository.findById(foodMenuItemId);
            if (foodMenuItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty foodMenuItemOpt received for foodMenuItemId: "+foodMenuItemId);
                return parentResponse;
            }
            FoodMenuItem foodMenuItem = foodMenuItemOpt.get();
            if (modifyFoodMenuItemRequest.getPrice() != null) {
                foodMenuItem.setPrice(modifyFoodMenuItemRequest.getPrice());
            }
            foodMenuItemRepository.save(foodMenuItem);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            BaseDBOperationsResponse.BaseDBOperationsResponseResponseData response =  parentResponse.getData();
            response.setSuccess(true);
            return  parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }
}
