package com.biplab.dholey.rmp.transformers;

import com.biplab.dholey.rmp.models.api.response.RestaurantRecipeControllerFetchAllRecipesResponse;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RecipeItemsToRestaurantRecipeControllerFetchAllRecipesResponseTransformer implements TransformerInterface<List<RecipeItem>, RestaurantRecipeControllerFetchAllRecipesResponse> {

    @Override
    public RestaurantRecipeControllerFetchAllRecipesResponse transform(List<RecipeItem> source) {
        RestaurantRecipeControllerFetchAllRecipesResponse response = new RestaurantRecipeControllerFetchAllRecipesResponse();
        List<RestaurantRecipeControllerFetchAllRecipesResponse.RestaurantRecipeControllerFetchAllRecipesResponseResponseData.Recipe> recipes = new ArrayList<>();
        for (RecipeItem recipeItem : source) {
            RestaurantRecipeControllerFetchAllRecipesResponse.RestaurantRecipeControllerFetchAllRecipesResponseResponseData.Recipe recipe = new RestaurantRecipeControllerFetchAllRecipesResponse.RestaurantRecipeControllerFetchAllRecipesResponseResponseData.Recipe();
            recipe.setName(recipeItem.getName());
            recipe.setDescription(recipeItem.getDescription());
            recipe.setInstruction(recipeItem.getRecipeInstruction());
            recipes.add(recipe);
        }
        if (!recipes.isEmpty()) {
            response.setData(new RestaurantRecipeControllerFetchAllRecipesResponse.RestaurantRecipeControllerFetchAllRecipesResponseResponseData());
            response.getData().setRecipesList(recipes);
        }
        return response;
    }
}
