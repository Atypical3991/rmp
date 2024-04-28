package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KitchenCookControllerPrepareFoodResponse  extends BaseResponse {

    @JsonProperty("data")
    private KitchenCookControllerPrepareFoodResponseResponseData data;

    @Data
    public static class KitchenCookControllerPrepareFoodResponseResponseData {
        @JsonProperty("prepareFoodRequestAccepted")
        private Boolean accepted;
    }


}
