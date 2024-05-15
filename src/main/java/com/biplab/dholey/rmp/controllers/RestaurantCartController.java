package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerAddItemRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerRemoveItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantCartControllerFetchActiveCartItemsByTableIdResponse;
import com.biplab.dholey.rmp.services.RestaurantCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/restaurant-cart")
public class RestaurantCartController {

    @Autowired
    private RestaurantCartService restaurantCartService;

    public ResponseEntity<BaseDBOperationsResponse> addFoodItemIntoCart(@RequestBody RestaurantCartControllerAddItemRequest restaurantCartControllerAddItemRequest) {
        return ResponseEntity.ok().body(restaurantCartService.addFoodItemIntoCart(restaurantCartControllerAddItemRequest));
    }

    public ResponseEntity<BaseDBOperationsResponse> removeFoodItemFromCart(@RequestBody RestaurantCartControllerRemoveItemRequest restaurantCartControllerRemoveItemRequest) {
        return ResponseEntity.ok().body(restaurantCartService.removeFoodItemFromCart(restaurantCartControllerRemoveItemRequest));
    }

    public ResponseEntity<RestaurantCartControllerFetchActiveCartItemsByTableIdResponse> fetchActiveCartItemsByTableId(@RequestParam(name = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantCartService.fetchActiveCartItemsByTableId(tableId));
    }

    public ResponseEntity<BaseDBOperationsResponse> discardCartByTableId(@RequestParam(name = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantCartService.discardCartByTableId(tableId));
    }
}
