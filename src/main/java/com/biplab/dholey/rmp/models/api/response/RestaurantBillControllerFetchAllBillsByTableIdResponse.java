package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantBillControllerFetchAllBillsByTableIdResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData data;

    @Override
    public RestaurantBillControllerFetchAllBillsByTableIdResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantBillControllerFetchAllBillsByTableIdResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantBillControllerFetchAllBillsByTableIdResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData {
        @JsonProperty("bills")
        private List<Bill> bills;

        @Data
        public static class Bill {
            @JsonProperty("billId")
            private Long billId;
            @JsonProperty("status")
            private String status;
        }
    }

}
