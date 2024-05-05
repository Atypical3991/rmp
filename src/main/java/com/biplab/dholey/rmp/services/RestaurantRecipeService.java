package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerAddRecipeRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerModifyRecipeRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantRecipeControllerFetchAllRecipesResponse;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.repositories.RecipeItemRepository;
import com.biplab.dholey.rmp.transformers.RecipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer;
import com.biplab.dholey.rmp.transformers.RestaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RestaurantRecipeService {

    @Autowired
    private RecipeItemRepository recipeItemRepository;

    @Autowired
    private RestaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer restaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer;

    @Autowired
    private RecipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer recipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer;

    public RecipeItem getRecipeItemById(Long recipeItemId) {
        Optional<RecipeItem> recipeItemOpt = recipeItemRepository.findById(recipeItemId);
        return recipeItemOpt.orElse(null);
    }

    public BaseDBOperationsResponse addRecipe(RestaurantRecipeControllerAddRecipeRequest addRecipeRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            RecipeItem recipeItem = restaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer.transform(addRecipeRequest);
            recipeItemRepository.save(recipeItem);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse deleteRecipe(Long recipeId) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            if (recipeItemRepository.existsById(recipeId)) {
                recipeItemRepository.deleteById(recipeId);
            }
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse modifyRecipe(RestaurantRecipeControllerModifyRecipeRequest modifyRecipeRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            if (modifyRecipeRequest.getRecipeId() == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_MODIFIED.value());
                parentResponse.setError("RecipeId missing in the request payload");
                return parentResponse;
            }
            Optional<RecipeItem> recipeItemOpt = recipeItemRepository.findById(modifyRecipeRequest.getRecipeId());
            if (recipeItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("RecipeItem not found for RecipeId: " + modifyRecipeRequest.getRecipeId() + ".");
                return parentResponse;
            }
            RecipeItem recipeItem = recipeItemOpt.get();
            if (modifyRecipeRequest.getName() != null) {
                recipeItem.setName(modifyRecipeRequest.getName());
            }
            if (modifyRecipeRequest.getDescription() != null) {
                recipeItem.setDescription(modifyRecipeRequest.getDescription());
            }
            if (modifyRecipeRequest.getInstruction() != null) {
                recipeItem.setRecipeInstruction(modifyRecipeRequest.getInstruction());
            }
            if (modifyRecipeRequest.getEstimatedTimeInMinutes() != null) {
                recipeItem.setEstimatedTimeInMinutes(modifyRecipeRequest.getEstimatedTimeInMinutes());
            }
            recipeItemRepository.save(recipeItem);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            return parentResponse;

        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public RestaurantRecipeControllerFetchAllRecipesResponse fetchAllRecipes() {
        RestaurantRecipeControllerFetchAllRecipesResponse parentResponse = new RestaurantRecipeControllerFetchAllRecipesResponse();
        try {
            List<RecipeItem> recipes = recipeItemRepository.findAll();
            RestaurantRecipeControllerFetchAllRecipesResponse response = recipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer.transform(recipes);
            response.setStatusCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }
}
