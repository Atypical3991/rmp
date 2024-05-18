package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FoodMenuItemControllerAddMenuItemRequest {

    @Positive(message = "recipeItemId should be greater than 0.")
    @JsonProperty("recipeId")
    private Long recipeItemId;

    @Positive(message = "price should be greater than 0.")
    @JsonProperty("price")
    private Double price;
}
