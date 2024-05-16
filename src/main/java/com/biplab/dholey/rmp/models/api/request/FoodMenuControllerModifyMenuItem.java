package com.biplab.dholey.rmp.models.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FoodMenuControllerModifyMenuItem {
    @JsonProperty("foodMenuItemId")
    private Long foodMenuItemId;
    @JsonProperty("price")
    private Double price;
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
}
