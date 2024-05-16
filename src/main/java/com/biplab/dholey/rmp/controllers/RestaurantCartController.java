package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerAddItemRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerRemoveItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantCartControllerFetchActiveCartItemsByTableIdResponse;
import com.biplab.dholey.rmp.services.RestaurantCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-cart")
public class RestaurantCartController {

    @Autowired
    private RestaurantCartService restaurantCartService;


    @PostMapping("/add-food-item-to-cart")
    public ResponseEntity<BaseDBOperationsResponse> addFoodItemIntoCart(@RequestBody RestaurantCartControllerAddItemRequest restaurantCartControllerAddItemRequest) {
        return ResponseEntity.ok().body(restaurantCartService.addFoodItemIntoCart(restaurantCartControllerAddItemRequest));
    }

    @PutMapping("/remove-food-item-from-cart")
    public ResponseEntity<BaseDBOperationsResponse> removeFoodItemFromCart(@RequestBody RestaurantCartControllerRemoveItemRequest restaurantCartControllerRemoveItemRequest) {
        return ResponseEntity.ok().body(restaurantCartService.removeFoodItemFromCart(restaurantCartControllerRemoveItemRequest));
    }

    @GetMapping("/fetch-cart-items-by-table-id")
    public ResponseEntity<RestaurantCartControllerFetchActiveCartItemsByTableIdResponse> fetchActiveCartItemsByTableId(@RequestParam(name = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantCartService.fetchActiveCartItemsByTableId(tableId));
    }


    @PutMapping("/discard-cart-items-by-table-id")
    public ResponseEntity<BaseDBOperationsResponse> discardCartByTableId(@RequestParam(name = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantCartService.discardCartByTableId(tableId));
    }
}
