package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.TableBookedItem;
import com.biplab.dholey.rmp.models.db.enums.BookedTableStatusEnum;
import com.biplab.dholey.rmp.repositories.TableBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RestaurantTableBookService {

    @Autowired
    private TableBookRepository tableBookRepository;

    public Boolean updateLastOrderServedAt(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setLastOrderServedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean updateLastOrderPlacedAt(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setLastOrderPlacedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean updateBillGenerateAt(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setBillGenerateAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Boolean updatePaymentReceivedAt(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setPaymentReceivedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public TableBookedItem getTableBookedItemByTableId(Long tableId) {
        return tableBookRepository.findByTableId(tableId);
    }

    public Boolean updateOrderIdsInTabledBookedItem(Long tableId, List<Long> orderIds) {
        TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
        if (tableBookedItem == null) {
            return false;
        }
        tableBookedItem.getOrderIds().addAll(orderIds);
        tableBookRepository.save(tableBookedItem);
        return true;
    }

    public boolean createBookTableItem(Long tableId) {
        try {
            TableBookedItem tableBookedItem = new TableBookedItem();
            tableBookedItem.setTableId(tableId);
            tableBookedItem.setTableBookedAt(LocalDateTime.now());
            tableBookedItem.setStatus(BookedTableStatusEnum.BOOKED);
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public boolean updateUnBookStatus(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            if (tableBookedItem.getStatus() != BookedTableStatusEnum.READY_TO_BE_UN_BOOKED) {
                throw new RuntimeException("Table can't be un-booked, as table with tableId:" + tableId +
                        " not in " + BookedTableStatusEnum.READY_TO_BE_UN_BOOKED.name() + " state.");
            }
            tableBookedItem.setTableUnBookedAt(LocalDateTime.now());
            tableBookedItem.setStatus(BookedTableStatusEnum.UN_BOOKED);
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

}
