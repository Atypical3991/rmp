package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
public class RestaurantTableControllerFetchTableStatusResponse extends BaseResponse {
    @JsonProperty("data")
    private RestaurantTableControllerFetchTableStatusResponseResponseData data;

    @Override
    public RestaurantTableControllerFetchTableStatusResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchTableStatusResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchTableStatusResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantTableControllerFetchTableStatusResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantTableControllerFetchTableStatusResponseResponseData {
        @JsonProperty("status")
        private String status;
    }

}
