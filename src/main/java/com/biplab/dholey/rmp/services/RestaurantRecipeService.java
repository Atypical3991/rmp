package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerAddRecipeRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerModifyRecipeRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantRecipeControllerFetchAllRecipesResponse;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import com.biplab.dholey.rmp.repositories.RecipeItemRepository;
import com.biplab.dholey.rmp.transformers.RecipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer;
import com.biplab.dholey.rmp.transformers.RestaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RestaurantRecipeService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantRecipeService.class));
    @Autowired
    private RecipeItemRepository recipeItemRepository;
    @Autowired
    private RestaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer restaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer;
    @Autowired
    private RecipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer recipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer;

    public RecipeItem fetchRecipeItemById(Long recipeItemId) {
        logger.info("getRecipeItemById called!!", "getRecipeItemById", RestaurantRecipeService.class.toString(), Map.of("recipeItemId", recipeItemId.toString()));
        Optional<RecipeItem> recipeItemOpt = recipeItemRepository.findById(recipeItemId);
        return recipeItemOpt.orElse(null);
    }

    public BaseDBOperationsResponse addRecipe(RestaurantRecipeControllerAddRecipeRequest addRecipeRequest) {
        try {
            logger.info("addRecipe called!!", "addRecipe", RestaurantRecipeService.class.toString(), Map.of("addRecipeRequest", addRecipeRequest.toString()));
            RecipeItem recipeItemFromDB = recipeItemRepository.findByName(addRecipeRequest.getName());
            if (recipeItemFromDB != null) {
                logger.info("Recipe by this name already added!!", "addRecipe", RestaurantRecipeService.class.toString(), Map.of("addRecipeRequest", addRecipeRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Recipe by this name already added.");
            }
            RecipeItem recipeItem = restaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer.transform(addRecipeRequest);
            recipeItemRepository.save(recipeItem);
            logger.info("addRecipe successfully processed!!", "addRecipe", RestaurantRecipeService.class.toString(), Map.of("addRecipeRequest", addRecipeRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "addRecipe successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in addRecipe!!", "addRecipe", RestaurantRecipeService.class.toString(), e, Map.of("addRecipeRequest", addRecipeRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse(" Internal server error", e);
        }
    }

    public BaseDBOperationsResponse deleteRecipe(Long recipeId) {
        try {
            logger.info("deleteRecipe called!!", "deleteRecipe", RestaurantRecipeService.class.toString(), Map.of("recipeId", recipeId.toString()));
            if (recipeItemRepository.existsById(recipeId)) {
                recipeItemRepository.deleteById(recipeId);
            }
            logger.info("deleteRecipe successfully processed!!", "deleteRecipe", RestaurantRecipeService.class.toString(), Map.of("recipeId", recipeId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "deleteRecipe successfully processed.");
        } catch (Exception e) {
            logger.info("Exception raised in deleteRecipe!!", "deleteRecipe", RestaurantRecipeService.class.toString(), Map.of("recipeId", recipeId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse modifyRecipe(RestaurantRecipeControllerModifyRecipeRequest modifyRecipeRequest) {
        try {
            logger.info("modifyRecipe called!!", "modifyRecipe", RestaurantRecipeService.class.toString(), Map.of("modifyRecipeRequest", modifyRecipeRequest.toString()));
            Optional<RecipeItem> recipeItemOpt = recipeItemRepository.findById(modifyRecipeRequest.getRecipeId());
            if (recipeItemOpt.isEmpty()) {
                logger.info("Recipe item not found!!", "modifyRecipe", RestaurantRecipeService.class.toString(), Map.of("modifyRecipeRequest", modifyRecipeRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("Recipe item not found.");
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
            logger.info("modifyRecipe successfully processed!!", "modifyRecipe", RestaurantRecipeService.class.toString(), Map.of("modifyRecipeRequest", modifyRecipeRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "modifyRecipe successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in modifyRecipe!!", "modifyRecipe", RestaurantRecipeService.class.toString(), e, Map.of("modifyRecipeRequest", modifyRecipeRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public RestaurantRecipeControllerFetchAllRecipesResponse fetchAllRecipes() {
        try {
            List<RecipeItem> recipes = recipeItemRepository.findAll();
            RestaurantRecipeControllerFetchAllRecipesResponse response = recipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer.transform(recipes);
            response.setStatusCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            return new RestaurantRecipeControllerFetchAllRecipesResponse().getInternalServerErrorResponse("Internal server error.", e);
        }
    }
}
