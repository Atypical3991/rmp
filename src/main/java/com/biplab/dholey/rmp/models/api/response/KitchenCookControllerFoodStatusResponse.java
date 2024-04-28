package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitchenCookControllerFoodStatusResponse extends BaseResponse {

    @JsonProperty("data")
    private KitchenCookControllerFoodStatusResponseResponseData data;

    @Data
    public static class KitchenCookControllerFoodStatusResponseResponseData {
        @JsonProperty
        private String status;
    }

}
