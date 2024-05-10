package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.TableBookedItem;
import com.biplab.dholey.rmp.models.db.enums.BookedTableStatusEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.print.Pageable;
import java.util.List;

public interface TableBookRepository extends JpaRepository<TableBookedItem, Long> {

    TableBookedItem findByTableId(Long tableId);

}
