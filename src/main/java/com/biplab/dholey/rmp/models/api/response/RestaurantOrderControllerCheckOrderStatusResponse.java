package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantOrderControllerCheckOrderStatusResponse  extends BaseResponse {
    @JsonProperty("data")
    private RestaurantOrderControllerCheckOrderStatusResponseResponseData data;

    @Data
    public static class RestaurantOrderControllerCheckOrderStatusResponseResponseData {
        @JsonProperty("orderStatus")
        private String status;
    }

}
