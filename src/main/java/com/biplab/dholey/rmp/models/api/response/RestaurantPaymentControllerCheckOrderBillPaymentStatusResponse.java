package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantPaymentControllerCheckOrderBillPaymentStatusResponseResponseData data;

    @Override
    public RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantPaymentControllerCheckOrderBillPaymentStatusResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantPaymentControllerCheckOrderBillPaymentStatusResponseResponseData {
        @JsonProperty("status")
        private String status;
    }

}
