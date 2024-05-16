package com.biplab.dholey.rmp.models.api.response;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantTableControllerFetchAllBookedTablesResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantTableControllerFetchAllBookedTablesResponseData data;

    @Override
    public RestaurantTableControllerFetchAllBookedTablesResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchAllBookedTablesResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchAllBookedTablesResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantTableControllerFetchAllBookedTablesResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantTableControllerFetchAllBookedTablesResponseData {
        @JsonProperty("tablesList")
        private List<TableItem> tablesList;

        @Data
        public static class TableItem {
            @JsonProperty("tableId")
            private Long tableId;

            @JsonProperty("tableNumber")
            private Long tableNumber;

            @JsonProperty("occupancy")
            private Long occupancy;
        }

    }


}
