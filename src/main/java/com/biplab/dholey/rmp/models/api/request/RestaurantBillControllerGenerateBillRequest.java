package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class RestaurantBillControllerGenerateBillRequest {
    @NotEmpty(message = "orderIds list can't be empty.")
    @JsonProperty("orderIds")
    private List<Long> orderIds = new ArrayList<>();

    @Positive(message = "tableItemId should be greater than 0.")
    @JsonProperty("tableItemId")
    private Long tableItemId;
}
