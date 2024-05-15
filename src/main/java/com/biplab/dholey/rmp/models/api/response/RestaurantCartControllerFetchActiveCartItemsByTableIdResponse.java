package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantCartControllerFetchActiveCartItemsByTableIdResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantCartControllerFetchActiveCartItemsByTableIdResponseData data;

    @Override
    public RestaurantCartControllerFetchActiveCartItemsByTableIdResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantCartControllerFetchActiveCartItemsByTableIdResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantCartControllerFetchActiveCartItemsByTableIdResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantCartControllerFetchActiveCartItemsByTableIdResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    public RestaurantCartControllerFetchActiveCartItemsByTableIdResponse getNotAcceptableServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
        return this;
    }

    @Data
    public static class RestaurantCartControllerFetchActiveCartItemsByTableIdResponseData {
        @JsonProperty("foodItems")
        private List<RestaurantCartControllerFetchActiveCartItemsByTableIdResponseFoodItem> foodItemList;
        @JsonProperty("totalPrice")
        private Double totalPrice;
    }

    @Data
    public static class RestaurantCartControllerFetchActiveCartItemsByTableIdResponseFoodItem {
        @JsonProperty()
        private String foodItemName;
        private Double foodItemPrice;
        private Long quantity;
    }


}
