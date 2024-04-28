package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitchenStockControllerRequestRefillRequest {
    @JsonProperty("itemName")
    private String name;

    @JsonProperty("itemQuantity")
    private String quantity;

    @JsonProperty("itemQuantityMetric")
    private String metric;
}
