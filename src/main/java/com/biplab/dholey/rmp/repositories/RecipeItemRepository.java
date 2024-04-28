package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.PaymentItem;
import com.biplab.dholey.rmp.models.db.RecipeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipeItemRepository extends JpaRepository<RecipeItem, Long> {
}
