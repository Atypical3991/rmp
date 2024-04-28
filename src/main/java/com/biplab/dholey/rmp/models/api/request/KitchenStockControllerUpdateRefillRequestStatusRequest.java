package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitchenStockControllerUpdateRefillRequestStatusRequest {
    @JsonProperty("refillRequestId")
    private Long refillRequestId;
    @JsonProperty("status")
    private String status;
}

