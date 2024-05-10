package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchTableStatusResponse;
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
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse bookTableByTableNumber(Long tableNumber) {
        try {
            logger.info("bookTableByTableNumber called!!", "bookTableByTableNumber", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem  not found.", "bookTableByTableNumber", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("tableItem not found.");
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
            tableItemRepository.save(tableItem);
            if (!restaurantTableBookService.createBookTableItem(tableItem.getId())) {
                logger.error("createBookTableItem failed!!", "bookTableByTableNumber", RestaurantTableService.class.toString(), new RuntimeException("createBookTableItem call failed!!"), Map.of("tableNumber", tableNumber.toString()));
                throw new RuntimeException("Table book item creation failed!! tableId: " + tableItem.getId());
            }
            logger.info("bookTableByTableNumber processed successfully!!", "bookTableByTableNumber", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "bookTableByTableNumber successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in bookTableByTableNumber.", "bookTableByTableNumber", RestaurantTableService.class.toString(), e, Map.of("tableNumber", tableNumber.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse bookTableByOccupancy(Long occupancy) {
        try {
            logger.info("bookTableByOccupancy called!!", "bookTableByOccupancy", RestaurantTableService.class.toString(), Map.of("occupancy", occupancy.toString()));
            TableItem tableItem = tableItemRepository.findByOccupancyGreaterThanEqualAndStatusOrderByOccupancyDesc(occupancy, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "bookTableByOccupancy", RestaurantTableService.class.toString(), Map.of("occupancy", occupancy.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("tableItem not found");
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
            tableItemRepository.save(tableItem);

            if (!restaurantTableBookService.createBookTableItem(tableItem.getId())) {
                logger.error("createBookTableItem call failed!!", "bookTableByOccupancy", RestaurantTableService.class.toString(), new RuntimeException("createBookTableItem call failed!!"), Map.of("occupancy", occupancy.toString()));
                throw new RuntimeException("Table book item creation failed!! tableId: " + tableItem.getId());
            }
            logger.info("bookTableByOccupancy processed successfully.", "bookTableByOccupancy", RestaurantTableService.class.toString(), Map.of("occupancy", occupancy.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "bookTableByOccupancy processed successfully.");
        } catch (Exception e) {
            logger.error("Exception raised in bookTableByOccupancy!!", "bookTableByOccupancy", RestaurantTableService.class.toString(), e, Map.of("occupancy", occupancy.toString()));
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
    public BaseDBOperationsResponse removeTable(Long tableNumber) {
        try {
            logger.info("removeTable called!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("tableItem not found.");
            }
            tableItem.setStatus(TableItemStatusEnum.REMOVED);
            tableItemRepository.save(tableItem);
            logger.info("removeTable processed successfully!!", "removeTable", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "removeTable processed successfully");
        } catch (Exception e) {
            logger.error("Exception raised in removeTable!!", "removeTable", RestaurantTableService.class.toString(), e, Map.of("tableNumber", tableNumber.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RestaurantTableControllerFetchTableStatusResponse fetchTableStatus(Long tableNumber) {
        try {
            logger.info("fetchTableStatus called successfully!!", "fetchTableStatus", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                logger.info("tableItem not found!!", "fetchTableStatus", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
                return new RestaurantTableControllerFetchTableStatusResponse().getNotFoundServerErrorResponse("tableItem not found");
            }
            TableItemStatusEnum tableItemStatusEnum = tableItem.getStatus();
            RestaurantTableControllerFetchTableStatusResponse.RestaurantTableControllerFetchTableStatusResponseResponseData data = new RestaurantTableControllerFetchTableStatusResponse.RestaurantTableControllerFetchTableStatusResponseResponseData();
            data.setStatus(tableItemStatusEnum.name());
            logger.info("fetchTableStatus processed successfully!!", "fetchTableStatus", RestaurantTableService.class.toString(), Map.of("tableNumber", tableNumber.toString()));
            return new RestaurantTableControllerFetchTableStatusResponse().getSuccessResponse(data, "fetchTableStatus processed successfully");

        } catch (Exception e) {
            logger.error("Exception raised in fetchTableStatus!!", "fetchTableStatus", RestaurantTableService.class.toString(), e, Map.of("tableNumber", tableNumber.toString()));
            return new RestaurantTableControllerFetchTableStatusResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public RestaurantTableControllerFetchAllAvailableTablesResponse fetchAllTables() {
        try {
            logger.info("fetchAllTables called!!", "fetchAllTables", RestaurantTableService.class.toString(), null);
            List<TableItem> tableItemsList = tableItemRepository.findAllByStatusIn(List.of(TableItemStatusEnum.AVAILABLE));
            if (tableItemsList.isEmpty()) {
                logger.info("No tableItem found!!", "fetchAllTables", RestaurantTableService.class.toString(), null);
                new RestaurantTableControllerFetchAllAvailableTablesResponse().getNotFoundServerErrorResponse("Table item not found");
            }
            RestaurantTableControllerFetchAllAvailableTablesResponse response = tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer.transform(tableItemsList);
            response.setStatusCode(HttpStatus.OK.value());
            logger.info("fetchAllTables successfully!!", "fetchAllTables", RestaurantTableService.class.toString(), null);
            return response;
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllTables!!", "fetchAllTables", RestaurantTableService.class.toString(), e, null);
            return new RestaurantTableControllerFetchAllAvailableTablesResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

}
