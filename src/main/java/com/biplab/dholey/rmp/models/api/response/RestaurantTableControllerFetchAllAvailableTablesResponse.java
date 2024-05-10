package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class RestaurantTableControllerFetchAllAvailableTablesResponse extends BaseResponse {
    @JsonProperty("data")
    private RestaurantTableControllerFetchAllAvailableTablesResponseResponseData data;

    @Override
    public RestaurantTableControllerFetchAllAvailableTablesResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchAllAvailableTablesResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public RestaurantTableControllerFetchAllAvailableTablesResponse getSuccessResponse(Object data, String message) {
        this.setData((RestaurantTableControllerFetchAllAvailableTablesResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class RestaurantTableControllerFetchAllAvailableTablesResponseResponseData {
        @JsonProperty("tables")
        private List<Table> tablesList;

        @Data
        public static class Table {
            @JsonProperty("tableNumber")
            private Long number;
            @JsonProperty("tableStatus")
            private String status;
            @JsonProperty("tableOccupancy")
            private Long occupancy;
        }
    }


}
