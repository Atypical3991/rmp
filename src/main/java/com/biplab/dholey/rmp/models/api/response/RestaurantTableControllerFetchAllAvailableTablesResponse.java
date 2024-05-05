package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantTableControllerFetchAllAvailableTablesResponse extends BaseResponse {
    @JsonProperty("data")
    private RestaurantTableControllerFetchAllAvailableTablesResponseResponseData data;

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
