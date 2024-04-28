package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FoodMenuControllerFetchFoodMenuResponse   extends BaseResponse {

    @JsonProperty("data")
    private FoodMenuControllerFetchFoodMenuResponseResponseData data;

    @Data
    public static class FoodMenuControllerFetchFoodMenuResponseResponseData {
        @JsonProperty("menuItems")
        private List<MenuItem> menuItems;

        @Data
        public static class MenuItem {
            @JsonProperty("name")
            private String name;
            @JsonProperty("description")
            private String description;
            @JsonProperty("price")
            private Double price;
        }
    }

}
