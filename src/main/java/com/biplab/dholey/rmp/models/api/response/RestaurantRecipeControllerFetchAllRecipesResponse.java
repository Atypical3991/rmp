package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantRecipeControllerFetchAllRecipesResponse  extends BaseResponse {

    @JsonProperty("data")
    private RestaurantRecipeControllerFetchAllRecipesResponseResponseData data;

    @Data
    public static class RestaurantRecipeControllerFetchAllRecipesResponseResponseData {
        @JsonProperty("recipes")
        private List<Recipe> recipesList;

        @Data
        public static class Recipe{
            @JsonProperty("name")
            private String name;
            @JsonProperty("description")
            private String description;
            @JsonProperty("instruction")
            private String instruction;
        }
    }

}
