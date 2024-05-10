package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestaurantOrderControllerCheckOrderStatusResponse extends BaseResponse {
    @JsonProperty("data")
    private RestaurantOrderControllerCheckOrderStatusResponseResponseData data;

    @Override
    public RestaurantOrderControllerCheckOrderStatusResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantOrderControllerCheckOrderStatusResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantOrderControllerCheckOrderStatusResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantOrderControllerCheckOrderStatusResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantOrderControllerCheckOrderStatusResponseResponseData {
        @JsonProperty("orderStatus")
        private String status;
    }

}
