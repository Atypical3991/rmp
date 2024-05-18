package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantOrderControllerCreateOrderRequest {
    @Positive(message = "tableId should be greater than 0.")
    @JsonProperty("tableId")
    private Long tableId;
}
