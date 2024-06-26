package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantOrderControllerFetchAllOrdersByTableResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData data;

    @Override
    public RestaurantOrderControllerFetchAllOrdersByTableResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantOrderControllerFetchAllOrdersByTableResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantOrderControllerFetchAllOrdersByTableResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

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
