package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.TableBookedItem;
import com.biplab.dholey.rmp.models.db.enums.BookedTableStatusEnum;
import com.biplab.dholey.rmp.repositories.TableBookRepository;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class RestaurantTableBookService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantTableBookService.class));
    @Autowired
    private TableBookRepository tableBookRepository;

    public Boolean updateLastOrderServedAt(Long tableId) {
        try {
            logger.info("updateLastOrderServedAt called!!", "updateLastOrderServedAt", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setLastOrderServedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateLastOrderServedAt!!", "updateLastOrderServedAt", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }
    }

    public Boolean updateLastOrderPlacedAt(Long tableId) {
        try {
            logger.info("updateLastOrderPlacedAt called!!", "updateLastOrderPlacedAt", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setLastOrderPlacedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateLastOrderPlacedAt!!", "updateLastOrderPlacedAt", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }
    }

    public Boolean updateBillGenerateAt(Long tableId) {
        try {
            logger.info("updateBillGenerateAt called!!", "updateBillGenerateAt", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setBillGenerateAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateBillGenerateAt!!", "updateBillGenerateAt", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }
    }

    public Boolean updatePaymentReceivedAt(Long tableId) {
        try {
            logger.info("updatePaymentReceivedAt called!!", "updatePaymentReceivedAt", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setPaymentReceivedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updatePaymentReceivedAt!!", "updatePaymentReceivedAt", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }
    }

    public TableBookedItem getTableBookedItemByTableId(Long tableId) {
        logger.info("getTableBookedItemByTableId called!!", "getTableBookedItemByTableId", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
        return tableBookRepository.findByTableId(tableId);
    }

    public Boolean updateOrderIdsInTabledBookedItem(Long tableId, List<Long> orderIds) {
        try {
            logger.info("updateOrderIdsInTabledBookedItem called!!", "updateOrderIdsInTabledBookedItem", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString(), "orderIds", orderIds.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            if (tableBookedItem == null) {
                logger.info("tableBookedItem not found!!", "updateOrderIdsInTabledBookedItem", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString(), "orderIds", orderIds.toString()));
                return false;
            }
            tableBookedItem.getOrderIds().addAll(orderIds);
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateOrderIdsInTabledBookedItem!!", "updateOrderIdsInTabledBookedItem", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString(), "orderIds", orderIds.toString()));
            return false;
        }

    }

    public boolean createBookTableItem(Long tableId) {
        try {
            logger.info("createBookTableItem called!!", "createBookTableItem", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = new TableBookedItem();
            tableBookedItem.setTableId(tableId);
            tableBookedItem.setTableBookedAt(LocalDateTime.now());
            tableBookedItem.setStatus(BookedTableStatusEnum.BOOKED);
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in createBookTableItem!!", "createBookTableItem", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }

    }

    public boolean updateUnBookStatus(Long tableId) {
        try {
            logger.info("updateUnBookStatus called!!", "updateUnBookStatus", RestaurantTableBookService.class.toString(), Map.of("tableId", tableId.toString()));
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            if (tableBookedItem.getStatus() != BookedTableStatusEnum.READY_TO_BE_UN_BOOKED) {
                throw new RuntimeException("Table can't be un-booked, as table with tableId:" + tableId + " not in " + BookedTableStatusEnum.READY_TO_BE_UN_BOOKED.name() + " state.");
            }
            tableBookedItem.setTableUnBookedAt(LocalDateTime.now());
            tableBookedItem.setStatus(BookedTableStatusEnum.UN_BOOKED);
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateUnBookStatus!!", "updateUnBookStatus", RestaurantTableBookService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return false;
        }

    }

}
