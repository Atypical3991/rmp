package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantBillControllerGenerateBillRequest {
    @JsonProperty("orderIds")
    private List<Long> orderIds;
    @JsonProperty("tableItemId")
    private Long tableItemId;
}
