package com.biplab.dholey.rmp.models.api.request;

import lombok.Data;

@Data
public class RestaurantCartControllerRemoveItemRequest {
    private Long tableId;
    private Long foodMenuItemId;
}
