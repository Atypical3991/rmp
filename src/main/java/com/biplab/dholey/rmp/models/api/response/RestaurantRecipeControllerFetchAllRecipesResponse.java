package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantRecipeControllerFetchAllRecipesResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantRecipeControllerFetchAllRecipesResponseResponseData data;

    @Override
    public RestaurantRecipeControllerFetchAllRecipesResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantRecipeControllerFetchAllRecipesResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantRecipeControllerFetchAllRecipesResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantRecipeControllerFetchAllRecipesResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantRecipeControllerFetchAllRecipesResponseResponseData {
        @JsonProperty("recipes")
        private List<Recipe> recipesList;

        @Data
        public static class Recipe {
            @JsonProperty("name")
            private String name;
            @JsonProperty("description")
            private String description;
            @JsonProperty("instruction")
            private String instruction;
        }
    }

}
