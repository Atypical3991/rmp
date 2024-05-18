package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantCartControllerAddItemRequest {

    @Positive(message = "tableId should be greater than 0.")
    @JsonProperty("tableId")
    private Long tableId;

    @Positive(message = "foodMenuItemId should be greater than 0.")
    @JsonProperty("foodMenuItemId")
    private Long foodMenuItemId;

    @Positive(message = "quantity should be greater than 0.")
    @JsonProperty("quantity")
    private Long quantity;
}
