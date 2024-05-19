package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.FoodMenuControllerModifyMenuItem;
import com.biplab.dholey.rmp.models.api.request.FoodMenuItemControllerAddMenuItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.FoodMenuControllerFetchFoodMenuResponse;
import com.biplab.dholey.rmp.services.RestaurantFoodMenuService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-menu")
public class FoodMenuController {

    @Autowired
    RestaurantFoodMenuService restaurantFoodMenuService;

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/fetch-food-menu")
    public ResponseEntity<FoodMenuControllerFetchFoodMenuResponse> fetchFoodMenu() {
        return ResponseEntity.ok(restaurantFoodMenuService.fetchFoodMenuItem());
    }

    @PostMapping("/add-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> addMenuItem(@Valid @RequestBody FoodMenuItemControllerAddMenuItemRequest addFoodMenuItemRequest) {
        return ResponseEntity.ok(restaurantFoodMenuService.addFoodMenuItem(addFoodMenuItemRequest));
    }

    @PutMapping("/modify-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> modifyMenuItem(@Valid @RequestBody FoodMenuControllerModifyMenuItem modifyFoodMenuItemRequest) {
        return ResponseEntity.ok(restaurantFoodMenuService.modifyFoodMenuItem(modifyFoodMenuItemRequest));
    }

    @DeleteMapping("/remove-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> removeMenuItem(@Valid @Positive(message = "foodMenuId should be greater than 0.") @RequestParam(value = "foodMenuId") Long foodMenuId) {
        return ResponseEntity.ok(restaurantFoodMenuService.removeFoodMenuItem(foodMenuId));
    }

}
