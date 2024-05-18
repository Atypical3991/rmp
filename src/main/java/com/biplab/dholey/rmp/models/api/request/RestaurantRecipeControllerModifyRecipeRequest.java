package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RestaurantRecipeControllerModifyRecipeRequest {
    @NotNull(message = "recipeId shouldn't be null.")
    @Positive(message = "recipeId should be greater than 0.")
    @JsonProperty("recipeId")
    private Long recipeId;
    @NotBlank(message = "name shouldn't be blank.")
    @Size(min = 3, max = 100, message = "Char length of name should be between 3 and 100.")
    @JsonProperty("name")
    private String name;
    @NotBlank(message = "description shouldn't be blank.")
    @Size(min = 20, max = 500, message = "Char length of description should be between 20 and 500.")
    @JsonProperty("description")
    private String description;
    @NotBlank(message = "instruction shouldn't be blank.")
    @Size(min = 20, max = 5000, message = "Char length of instruction should be between 20 and 5000.")
    @JsonProperty("instruction")
    private String instruction;
    @Positive(message = "estimatedTimeInMinutes should bne greater than 0.")
    @JsonProperty("estimatedTimeInMinutes")
    private Long estimatedTimeInMinutes;
}
