package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantOrderControllerFetchAllOrdersByTableResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData data;

    @Data
    public static class RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData {
        @JsonProperty("orders")
        private List<Order> orders;

        @Data
        public static class Order {
            @JsonProperty("orderId")
            private Long orderId;

            @JsonProperty("orderStatus")
            private String orderStatus;
        }
    }


}
