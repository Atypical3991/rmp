package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.FoodItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FoodItemRepository extends JpaRepository<FoodItem, Long> {
}