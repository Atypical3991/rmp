package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.FoodMenuControllerModifyMenuItem;
import com.biplab.dholey.rmp.models.api.request.FoodMenuItemControllerAddMenuItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.FoodMenuControllerFetchFoodMenuResponse;
import com.biplab.dholey.rmp.services.FoodMenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/food-menu")
public class FoodMenuController {

    @Autowired
    FoodMenuService foodMenuService;

    @GetMapping("/fetch-food-menu")
    public ResponseEntity<FoodMenuControllerFetchFoodMenuResponse> fetchFoodMenu() {
        return ResponseEntity.ok(foodMenuService.fetchFoodMenuItem());
    }

    @PostMapping("/add-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> addMenuItem(@RequestBody FoodMenuItemControllerAddMenuItemRequest addFoodMenuItemRequest) {
        return ResponseEntity.ok(foodMenuService.addFoodMenuItem(addFoodMenuItemRequest));
    }

    @PutMapping("/modify-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> modifyMenuItem(@RequestBody FoodMenuControllerModifyMenuItem modifyFoodMenuItemRequest) {
        return ResponseEntity.ok(foodMenuService.modifyFoodMenuItem(modifyFoodMenuItemRequest));
    }

    @DeleteMapping("/remove-food-menu-item")
    public ResponseEntity<BaseDBOperationsResponse> removeMenuItem(@RequestParam(value = "foodMenuId") Long foodMenuId) {
        return ResponseEntity.ok(foodMenuService.removeFoodMenuItem(foodMenuId));
    }

}
