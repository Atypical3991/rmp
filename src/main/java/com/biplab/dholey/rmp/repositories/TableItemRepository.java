package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TableItemRepository extends JpaRepository<TableItem, Long> {
    TableItem findByIdAndStatus(Long tableId, TableItemStatusEnum status);

    TableItem findByTableNumber(Long tableId);

    List<TableItem> findAllByStatusIn(List<TableItemStatusEnum> statusEnumList);
}
