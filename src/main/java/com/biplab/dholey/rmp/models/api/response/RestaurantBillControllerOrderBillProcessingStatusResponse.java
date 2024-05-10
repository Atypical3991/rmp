package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestaurantBillControllerOrderBillProcessingStatusResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantBillControllerOrderBillProcessingStatusResponseResponseData data;

    @Override
    public RestaurantBillControllerOrderBillProcessingStatusResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantBillControllerOrderBillProcessingStatusResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantBillControllerOrderBillProcessingStatusResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantBillControllerOrderBillProcessingStatusResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantBillControllerOrderBillProcessingStatusResponseResponseData {
        @JsonProperty("billProcessingStatus")
        private String status;
    }

}
