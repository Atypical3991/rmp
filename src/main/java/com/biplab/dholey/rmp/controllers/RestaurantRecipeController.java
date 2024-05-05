package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerAddRecipeRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantRecipeControllerModifyRecipeRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantRecipeControllerFetchAllRecipesResponse;
import com.biplab.dholey.rmp.services.RestaurantRecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-recipe")
public class RestaurantRecipeController {

    @Autowired
    RestaurantRecipeService restaurantRecipeService;

    @PostMapping("/add-recipe")
    public ResponseEntity<BaseDBOperationsResponse> addRecipe(@RequestBody RestaurantRecipeControllerAddRecipeRequest addRecipeRequest) {
        return ResponseEntity.ok().body(restaurantRecipeService.addRecipe(addRecipeRequest));
    }

    @PutMapping("/delete-recipe")
    public ResponseEntity<BaseDBOperationsResponse> deleteRecipe(@RequestParam(value = "recipeId") Long recipeId) {
        return ResponseEntity.ok().body(restaurantRecipeService.deleteRecipe(recipeId));
    }

    @PutMapping("/modify-recipe")
    public ResponseEntity<BaseDBOperationsResponse> modifyRecipe(@RequestBody RestaurantRecipeControllerModifyRecipeRequest modifyRecipeRequest) {
        return ResponseEntity.ok().body(restaurantRecipeService.modifyRecipe(modifyRecipeRequest));
    }

    @GetMapping("/fetch-all-recipes")
    public ResponseEntity<RestaurantRecipeControllerFetchAllRecipesResponse> fetchAvailableRecipes() {
        return ResponseEntity.ok().body(restaurantRecipeService.fetchAllRecipes());
    }
}
