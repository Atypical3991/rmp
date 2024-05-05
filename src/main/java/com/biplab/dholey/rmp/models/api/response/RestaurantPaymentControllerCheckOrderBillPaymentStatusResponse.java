package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse extends BaseResponse {

    @JsonProperty("data")
    private RestaurantPaymentControllerCheckOrderBillPaymentStatusResponseResponseData data;

    @Data
    public static class RestaurantPaymentControllerCheckOrderBillPaymentStatusResponseResponseData {
        @JsonProperty("status")
        private String status;
    }

}
