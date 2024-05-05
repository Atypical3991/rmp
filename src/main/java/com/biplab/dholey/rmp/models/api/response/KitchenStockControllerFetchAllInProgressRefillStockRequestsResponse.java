package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse extends BaseResponse {

    @JsonProperty("data")
    private KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData data;

    @Data
    public static class KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData {
        @JsonProperty("refillStockRequests")
        private List<RefillStockRequest> refillStockRequests;

        @Data
        public static class RefillStockRequest {
            @JsonProperty("refillRequestId")
            private Long refillRequestId;
            @JsonProperty("status")
            private String status;
        }
    }

}
