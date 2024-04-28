package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodMenuItemRepository extends JpaRepository<FoodMenuItem, Long> {
    FoodMenuItem findByRecipeItemId(Long recipeItemId);
}