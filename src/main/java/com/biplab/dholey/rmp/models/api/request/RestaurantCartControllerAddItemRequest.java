package com.biplab.dholey.rmp.models.api.request;

import lombok.Data;

@Data
public class RestaurantCartControllerAddItemRequest {
    private Long tableId;
    private Long foodMenuItemId;
    private Long quantity;
}
