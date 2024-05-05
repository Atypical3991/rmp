package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RestaurantBillControllerFetchAllBillsByTableIdResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData data;

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
