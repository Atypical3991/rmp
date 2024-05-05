package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantBillControllerOrderBillProcessingStatusResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantBillControllerOrderBillProcessingStatusResponseResponseData data;

    @Data
    public static class RestaurantBillControllerOrderBillProcessingStatusResponseResponseData {
        @JsonProperty("billProcessingStatus")
        private String status;
    }

}
