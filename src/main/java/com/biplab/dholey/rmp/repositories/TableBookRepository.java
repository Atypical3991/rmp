package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.TableBookedItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableBookRepository  extends JpaRepository<TableBookedItem, Long> {

    TableBookedItem findByTableId(Long tableId);
}
