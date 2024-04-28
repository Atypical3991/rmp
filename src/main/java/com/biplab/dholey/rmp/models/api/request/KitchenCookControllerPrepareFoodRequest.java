package com.biplab.dholey.rmp.models.api.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitchenCookControllerPrepareFoodRequest {

    @JsonProperty("orderId")
    private Long orderId;
}
