package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantTableControllerAddTableRequest {
    @Positive(message = "tableNumber should be greater than 0.")
    @JsonProperty("tableNumber")
    private Long tableNumber;
    @Positive(message = "tableOccupancy should be greater than 0.")
    @JsonProperty("tableOccupancy")
    private Long tableOccupancy;
}
