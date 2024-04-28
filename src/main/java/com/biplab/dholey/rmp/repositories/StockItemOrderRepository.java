package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.StockItemOrder;
import com.biplab.dholey.rmp.models.db.enums.StockItemOrderStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockItemOrderRepository extends JpaRepository<StockItemOrder, Long> {
    List<StockItemOrder> findAllByStatusIn(List<StockItemOrderStatusEnum> statues);
}