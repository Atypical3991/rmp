package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantTableControllerAddTableRequest {
    @JsonProperty("tableNumber")
    private Long tableNumber;
    @JsonProperty("tableOccupancy")
    private Long tableOccupancy;
}
