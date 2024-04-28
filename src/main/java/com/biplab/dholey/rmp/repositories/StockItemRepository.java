package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface StockItemRepository extends JpaRepository<StockItem, Long> {

    List<StockItem> findAllByQuantityGreaterThan(Long quantity);
}
