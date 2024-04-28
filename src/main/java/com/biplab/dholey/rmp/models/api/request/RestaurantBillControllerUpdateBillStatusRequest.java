package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantBillControllerUpdateBillStatusRequest {
    @JsonProperty("billId")
    private Long billId;
    @JsonProperty("status")
    private String status;
}
