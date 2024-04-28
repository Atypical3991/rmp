package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KitchenStockControllerCheckCurrentStockResponse extends BaseResponse {

    @JsonProperty("data")
    private KitchenCookControllerPrepareFoodResponseResponseData data;

    @Data
    public static class KitchenCookControllerPrepareFoodResponseResponseData {
        @JsonProperty("stocks")
        private List<Stock> stocks;

        @Data
        public static class Stock {
            @JsonProperty("stockName")
            private String name;
            @JsonProperty("stockQuantity")
            private Long quantity;
            @JsonProperty("stockMetric")
            private String metric;
        }

    }


}
