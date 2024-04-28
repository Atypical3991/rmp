package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantOrderControllerCreateOrderRequest {
    @JsonProperty("orders")
    private List<Order> orderList;

    @Data
    public static class Order{
        @JsonProperty("itemId")
        private Long foodMenuItemId;
        @JsonProperty("itemQuantity")
        private Long quantity;
        @JsonProperty("tableId")
        private Long tableId;
    }
}
