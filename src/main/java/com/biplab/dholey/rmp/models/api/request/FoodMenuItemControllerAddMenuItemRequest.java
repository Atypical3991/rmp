package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FoodMenuItemControllerAddMenuItemRequest {
    @JsonProperty("recipeId")
    private Long recipeItemId;
    @JsonProperty("price")
    private Double price;
}
