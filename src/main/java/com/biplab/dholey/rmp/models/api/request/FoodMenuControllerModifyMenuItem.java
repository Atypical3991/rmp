package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FoodMenuControllerModifyMenuItem {

    @Positive(message = "foodMenuItemId should be greater than 0.")
    @JsonProperty("foodMenuItemId")
    private Long foodMenuItemId;

    @PositiveOrZero(message = "price should be greater than equal to 0.")
    @JsonProperty("price")
    private Double price;

    @NotNull(message = "name shouldn't be null.")
    @Size(min = 0, max = 100, message = "Char length of name should be between 0 and 100.")
    @JsonProperty("name")
    private String name;

    @NotNull(message = "description shouldn't be null.")
    @Size(min = 10, max = 500, message = "Char length of description should be between 10 and 500.")
    @JsonProperty("description")
    private String description;
}
