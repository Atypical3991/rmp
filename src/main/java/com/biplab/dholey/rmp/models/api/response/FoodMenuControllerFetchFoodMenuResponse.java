package com.biplab.dholey.rmp.models.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
public class FoodMenuControllerFetchFoodMenuResponse extends BaseResponse {

    @JsonProperty("data")
    private FoodMenuControllerFetchFoodMenuResponseResponseData data;

    @Override
    public BaseResponse getInternalServerErrorResponse(String message, Exception e) {
        this.setMessage(message);
        this.setError(e.getMessage());
        this.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return this;
    }

    @Override
    public BaseResponse getNotFoundServerErrorResponse(String message) {
        this.setMessage(message);
        this.setStatusCode(HttpStatus.NOT_FOUND.value());
        return this;
    }

    @Override
    public BaseResponse getSuccessResponse(Object data, String message) {
        this.setData((FoodMenuControllerFetchFoodMenuResponse.FoodMenuControllerFetchFoodMenuResponseResponseData) data);
        this.setMessage(message);
        this.setStatusCode(HttpStatus.OK.value());
        return this;
    }

    @Data
    public static class FoodMenuControllerFetchFoodMenuResponseResponseData {
        @JsonProperty("menuItems")
        private List<MenuItem> menuItems;

        @Data
        public static class MenuItem {
            @JsonProperty("id")
            private Long id;
            @JsonProperty("name")
            private String name;
            @JsonProperty("description")
            private String description;
            @JsonProperty("price")
            private Double price;
        }
    }
}
