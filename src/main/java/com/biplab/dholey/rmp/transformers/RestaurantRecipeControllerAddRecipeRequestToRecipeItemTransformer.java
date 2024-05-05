package com.biplab.dholey.rmp.transformers;

import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerAddRecipeRequest;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import org.springframework.stereotype.Component;

@Component
public class RestaurantRecipeControllerAddRecipeRequestToRecipeItemTransformer implements TransformerInterface<RestaurantRecipeControllerAddRecipeRequest, RecipeItem> {
    @Override
    public RecipeItem transform(RestaurantRecipeControllerAddRecipeRequest source) {
        RecipeItem recipeItem = new RecipeItem();
        recipeItem.setName(source.getName());
        recipeItem.setRecipeInstruction(source.getInstruction());
        recipeItem.setDescription(source.getDescription());
        recipeItem.setEstimatedTimeInMinutes(source.getEstimatedTimeInMinutes());
        return recipeItem;
    }
}
