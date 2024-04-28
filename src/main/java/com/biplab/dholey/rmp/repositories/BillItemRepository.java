package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillItemRepository extends JpaRepository<BillItem, Long> {
    BillItem findByIdAndStatus(Long id, BillItemStatusEnum status);

    Long countByOrderItemIdsIn(List<Long> orderIds);

    List<BillItem> findAllByTableItemId(Long tableId);
}