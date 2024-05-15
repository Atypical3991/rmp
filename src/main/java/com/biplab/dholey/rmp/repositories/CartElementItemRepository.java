package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.CartElementItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartElementItemRepository extends JpaRepository<CartElementItem, Long> {

    List<CartElementItem> findAllByIdIn(List<Long> ids);
}
