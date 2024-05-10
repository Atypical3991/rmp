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
import com.biplab.dholey.rmp.util.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
public class RestaurantTableService {

    private final TaskQueue tableCleaningTaskQueue = new TaskQueue("restaurant_table_cleaning_task_queue", 100);
    @Autowired
    private TableItemRepository tableItemRepository;

    @Autowired
    private RestaurantTableBookService restaurantTableBookService;


    @Autowired
    private RestaurantTableControllerAddTableRequestToTableItemTransformer restaurantTableControllerAddTableRequestToTableItemTransformer;


    private TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer;


    public BaseDBOperationsResponse addTable(RestaurantTableControllerAddTableRequest addTableRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableItem item = restaurantTableControllerAddTableRequestToTableItemTransformer.transform(addTableRequest);
            tableItemRepository.save(item);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse bookTableByTableNumber(Long tableNumber) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No table available for table number: " + tableNumber);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
            tableItemRepository.save(tableItem);
            if (!restaurantTableBookService.createBookTableItem(tableItem.getId())) {
                throw new RuntimeException("Table book item creation failed!! tableId: " + tableItem.getId());
            }
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse bookTableByOccupancy(Long occupancy) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByOccupancyGreaterThanEqualAndStatusOrderByOccupancyDesc(occupancy, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No table available for occupancy: " + occupancy);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
            tableItemRepository.save(tableItem);

            if (!restaurantTableBookService.createBookTableItem(tableItem.getId())) {
                throw new RuntimeException("Table book item creation failed!! tableId: " + tableItem.getId());
            }

            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse unBookTable(Long tableId) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {

            Optional<TableItem> tableItemOpt = tableItemRepository.findById(tableId);
            if (tableItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("NO booked table found for table id: " + tableId);
                return parentResponse;
            }
            TableItem tableItem = tableItemOpt.get();
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("NO booked table found for table id: " + tableId);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.AVAILABLE);
            tableItemRepository.save(tableItem);

            if (!restaurantTableBookService.updateUnBookStatus(tableId)) {
                throw new RuntimeException("Updating unBook status in table book item failed!! tableId: " + tableId);
            }

            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse removeTable(Long tableNumber) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No table found for table number :" + tableNumber);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.REMOVED);
            tableItemRepository.save(tableItem);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public RestaurantTableControllerFetchTableStatusResponse fetchTableStatus(Long tableNumber) {
        RestaurantTableControllerFetchTableStatusResponse parentResponse = new RestaurantTableControllerFetchTableStatusResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Table not available for tableNumber: " + tableNumber);
                return parentResponse;
            }
            TableItemStatusEnum tableItemStatusEnum = tableItem.getStatus();
            parentResponse.setData(new RestaurantTableControllerFetchTableStatusResponse.RestaurantTableControllerFetchTableStatusResponseResponseData());
            parentResponse.getData().setStatus(tableItemStatusEnum.name());
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public RestaurantTableControllerFetchAllAvailableTablesResponse fetchAllTables() {
        RestaurantTableControllerFetchAllAvailableTablesResponse parentResponse = new RestaurantTableControllerFetchAllAvailableTablesResponse();
        try {
            List<TableItem> tableItemsList = tableItemRepository.findAllByStatusIn(List.of(TableItemStatusEnum.AVAILABLE));
            if (tableItemsList.isEmpty()) {
                parentResponse.setError("tableItemsList not found");
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                return parentResponse;
            }
            RestaurantTableControllerFetchAllAvailableTablesResponse response = tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer.transform(tableItemsList);
            response.setStatusCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse cleanTable(Long tableId) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableCleanRequestTaskQueueModel tableCleanRequestTaskQueueModel = new TableCleanRequestTaskQueueModel();
            tableCleanRequestTaskQueueModel.setTableId(tableId);
            tableCleaningTaskQueue.pushTask(tableCleanRequestTaskQueueModel);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public TableCleanRequestTaskQueueModel popCleaningTableTask() {
        return (TableCleanRequestTaskQueueModel) tableCleaningTaskQueue.popTask();
    }

}
