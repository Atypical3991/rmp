package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantBillControllerFetchBillDetailsByBillIdResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantBillControllerFetchBillDetailsByBillIdResponseData data;

    @Override
    public RestaurantBillControllerFetchBillDetailsByBillIdResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantBillControllerFetchBillDetailsByBillIdResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantBillControllerFetchBillDetailsByBillIdResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantBillControllerFetchBillDetailsByBillIdResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantBillControllerFetchBillDetailsByBillIdResponseData {

        @JsonProperty("tableNumber")
        private Long tableNumber;

        @JsonProperty("ordersList")
        private List<OrderDetails> ordersList;

        @JsonProperty("totalPayable")
        private Double totalPayable;

        @JsonProperty("paymentStatus")
        private String paymentStatus;

    }

    @Data
    public static class OrderDetails {
        @JsonProperty("orderId")
        private Long orderId;
        @JsonProperty("foodItemName")
        private String foodItemName;
        @JsonProperty("totalPrice")
        private Double totalPrice;
    }
}
