package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantBillControllerCreateAndProcessOrderBillResponse extends BaseResponse {
    @JsonProperty("data")
    private RestaurantBillControllerCreateAndProcessOrderBillResponseResponseData data;

    @Data
    public static class RestaurantBillControllerCreateAndProcessOrderBillResponseResponseData {
        @JsonProperty("acceptanceStatus")
        private boolean acceptanceStatus;
    }

}
