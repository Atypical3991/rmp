package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllBookedTablesResponse;
import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.TableCleanRequestTaskQueueModel;
import com.biplab.dholey.rmp.repositories.TableItemRepository;
import com.biplab.dholey.rmp.transformers.RestaurantTableControllerAddTableRequestToTableItemTransformer;
import com.biplab.dholey.rmp.transformers.TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer;
import com.biplab.dholey.rmp.util.CustomLogger;
import com.biplab.dholey.rmp.util.CustomTaskQueue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
public class RestaurantTableService {
    private final CustomTaskQueue tableCleaningCustomTaskQueue = new CustomTaskQueue("restaurant_table_cleaning_task_queue", 100);
    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantTableService.class));
    @Autowired
    private TableItemRepository tableItemRepository;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;
    @Autowired
    private RestaurantTableControllerAddTableRequestToTableItemTransformer restaurantTableControllerAddTableRequestToTableItemTransformer;
    private TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer;

    public BaseDBOperationsResponse addTable(RestaurantTableControllerAddTableRequest addTableRequest) {
        try {
            logger.info("addTable called!!", "addTable", RestaurantTableService.class.toString(), Map.of("addTableRequest", addTableRequest.toString()));
            TableItem item = restaurantTableControllerAddTableRequestToTableItemTransformer.transform(addTableRequest);
            tableItemRepository.save(item);
            logger.info("addTable successfully processed!!", "addTable", RestaurantTableService.class.toString(), Map.of("addTableRequest", addTableRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "addTable successfully processed!!");
        } catch (Exception e) {
            logger.error("Exception raised in addTable!!", "addTable", RestaurantTableService.class.toString(), e, Map.of("addTableRequest", addTableRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error.", e);
        }
    }

    public TableCleanRequestTaskQueueModel popCleaningTableTask() {
        try {
            logger.info("popCleaningTableTask called!!", "popCleaningTableTask", RestaurantTableService.class.toString(), null);
            return (TableCleanRequestTaskQueueModel) tableCleaningCustomTaskQueue.popTask();
        } catch (Exception e) {
            logger.error("Exception raised in popCleaningTableTask", "popCleaningTableTask", RestaurantTableService.class.toString(), e, null);
            return null;
        }
    }

    public TableItem fetchTableById(Long tableId) {
        try {
            Optional<TableItem> tableItemOpt = tableItemRepository.findById(tableId);
            if (tableItemOpt.isEmpty()) {
                logger.error("fetchTableById called!!", "fetchTableById", RestaurantTableService.class.toString(), new RuntimeException("Table not found."), Map.of("tableId", tableId.toString()));
                return null;
            }
            return tableItemOpt.get();
        } catch (Exception e) {
            return null;
        }
    }


    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse bookTableByTableId(Long tableId) {
        try {
            logger.info("bookTableByOccupancy called!!", "bookTableByTableId", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            TableItem tableItem = tableItemRepository.findByIdAndStatus(tableId, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "bookTableByTableId", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("tableItem not found");
            }
            if (tableItem.getStatus() == TableItemStatusEnum.BOOKED) {
                logger.info("tableItem is in Booked state!!", "bookTableByTableId", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("tableItem is in Booked state!!");
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
            tableItemRepository.save(tableItem);

            if (!restaurantTableBookService.createBookTableItem(tableItem.getId())) {
                logger.error("createBookTableItem call failed!!", "bookTableByTableId", RestaurantTableService.class.toString(), new RuntimeException("createBookTableItem call failed!!"), Map.of("tableId", tableId.toString()));
                throw new RuntimeException("Table book item creation failed!! tableId: " + tableItem.getId());
            }
            logger.info("bookTableByOccupancy processed successfully.", "bookTableByTableId", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "bookTableByTableId processed successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in bookTableByTableId!!", "bookTableByTableId", RestaurantTableService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse unBookTable(Long tableId) {
        try {
            logger.info("unBookTable called!!", "unBookTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            Optional<TableItem> tableItemOpt = tableItemRepository.findById(tableId);
            if (tableItemOpt.isEmpty()) {
                logger.info("tableItem not found", "unBookTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("tableItem not found");
            }
            TableItem tableItem = tableItemOpt.get();
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                logger.info("tableItem not in BOOKED state", "unBookTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("tableItem not in BOOKED state.");
            }
            tableItem.setStatus(TableItemStatusEnum.AVAILABLE);
            tableItemRepository.save(tableItem);

            if (!restaurantTableBookService.updateUnBookStatus(tableId)) {
                logger.error("updateUnBookStatus failed", "unBookTable", RestaurantTableService.class.toString(), new RuntimeException("updateUnBookStatus failed"), Map.of("tableId", tableId.toString()));
                throw new RuntimeException("Updating unBook status in table book item failed!! tableId: " + tableId);
            }
            logger.info("unBookTable processed successfully", "unBookTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "unBookTable processed successfully.");
        } catch (Exception e) {
            logger.error("unBookTable processing failed", "unBookTable", RestaurantTableService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse removeTable(Long tableId) {
        try {
            logger.info("removeTable called!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            TableItem tableItem = tableItemRepository.findByIdAndStatus(tableId, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("tableItem not found.");
            }
            if (tableItem.getStatus() != TableItemStatusEnum.AVAILABLE) {
                logger.info("tableItem still in Booked state!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("tableItem still in Booked state.");
            }
            tableItem.setStatus(TableItemStatusEnum.REMOVED);
            tableItemRepository.save(tableItem);
            logger.info("removeTable processed successfully!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "removeTable processed successfully");
        } catch (Exception e) {
            logger.error("Exception raised in removeTable!!", "removeTable", RestaurantTableService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse cleanTable(Long tableId) {
        try {
            logger.info("cleanTable called!!", "cleanTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
            TableItem tableItem = tableItemRepository.findByIdAndStatus(tableId, TableItemStatusEnum.BOOKED);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "cleanTable", RestaurantTableService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("tableItem not found.");
            }

            if (!restaurantTableBookService.updateTableCleanRequestQueueStatus(tableId)) {
                throw new RuntimeException("updateTableCleanRequestQueueStatus call failed!!");
            }
            try {
                if (!tableCleaningCustomTaskQueue.pushTask(new TableCleanRequestTaskQueueModel(tableId))) {
                    logger.error("tableCleaningCustomTaskQueue task push failed!!", "cleanTable", RestaurantTableService.class.toString(), new RuntimeException("tableCleaningCustomTaskQueue task push failed."), Map.of("tableId", tableId.toString()));
                }
            } catch (Exception e) {
                logger.error("tableCleaningCustomTaskQueue task push failed!!", "cleanTable", RestaurantTableService.class.toString(), new RuntimeException("tableCleaningCustomTaskQueue task push failed."), Map.of("tableId", tableId.toString()));
            }
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "cleanTable processed successfully!!");
        } catch (Exception e) {
            logger.error("cleanTable call failed!!", "cleanTable", RestaurantTableService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public RestaurantTableControllerFetchAllAvailableTablesResponse fetchAllAvailableTables() {
        try {
            logger.info("fetchAllAvailableTables called!!", "fetchAllAvailableTables", RestaurantTableService.class.toString(), null);
            List<TableItem> tableItemsList = tableItemRepository.findAllByStatusIn(List.of(TableItemStatusEnum.AVAILABLE));
            if (tableItemsList.isEmpty()) {
                logger.info("No tableItem found!!", "fetchAllAvailableTables", RestaurantTableService.class.toString(), null);
                new RestaurantTableControllerFetchAllAvailableTablesResponse().getNotFoundServerErrorResponse("Table item not found");
            }
            RestaurantTableControllerFetchAllAvailableTablesResponse response = tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer.transform(tableItemsList);
            response.setStatusCode(HttpStatus.OK.value());
            logger.info("fetchAllAvailableTables successfully!!", "fetchAllAvailableTables", RestaurantTableService.class.toString(), null);
            return response;
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllTables!!", "fetchAllAvailableTables", RestaurantTableService.class.toString(), e, null);
            return new RestaurantTableControllerFetchAllAvailableTablesResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }


    public RestaurantTableControllerFetchAllBookedTablesResponse fetchBookedTables() {
        try {
            logger.info("fetchBookedTables called!!", "fetchBookedTables", RestaurantTableService.class.toString(), null);
            List<TableItem> tableItemsList = tableItemRepository.findAllByStatusIn(List.of(TableItemStatusEnum.BOOKED));
            if (tableItemsList.isEmpty()) {
                logger.info("No tableItem found!!", "fetchBookedTables", RestaurantTableService.class.toString(), null);
                new RestaurantTableControllerFetchAllBookedTablesResponse().getNotFoundServerErrorResponse("Table item not found");
            }
            RestaurantTableControllerFetchAllBookedTablesResponse.RestaurantTableControllerFetchAllBookedTablesResponseData data = new RestaurantTableControllerFetchAllBookedTablesResponse.RestaurantTableControllerFetchAllBookedTablesResponseData();
            data.setTablesList(new ArrayList<>());
            for (TableItem tableItem : tableItemsList) {
                RestaurantTableControllerFetchAllBookedTablesResponse.TableItem table = new RestaurantTableControllerFetchAllBookedTablesResponse.TableItem();
                table.setTableNumber(tableItem.getTableNumber());
                table.setOccupancy(tableItem.getOccupancy());
                data.getTablesList().add(table);
            }
            logger.info("fetchBookedTables successfully!!", "fetchAllTables", RestaurantTableService.class.toString(), null);
            return new RestaurantTableControllerFetchAllBookedTablesResponse().getSuccessResponse(data, "Booked tables fetched successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllTables!!", "fetchBookedTables", RestaurantTableService.class.toString(), e, null);
            return new RestaurantTableControllerFetchAllBookedTablesResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

}
