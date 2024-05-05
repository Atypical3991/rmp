package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableItemRepository extends JpaRepository<TableItem, Long> {
    TableItem findByTableNumberAndStatus(Long tableNumber, TableItemStatusEnum status);

    TableItem findByOccupancyGreaterThanEqualAndStatusOrderByOccupancyDesc(Long occupancy_from, TableItemStatusEnum status);

    List<TableItem> findAllByStatusIn(List<TableItemStatusEnum> statusEnumList);
}
