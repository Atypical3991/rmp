package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantTableControllerFetchTableStatusResponse  extends BaseResponse {
    @JsonProperty("data")
    private RestaurantTableControllerFetchTableStatusResponseResponseData data;

    @Data
    public static class RestaurantTableControllerFetchTableStatusResponseResponseData {
        @JsonProperty("status")
        private String status;
    }

}
