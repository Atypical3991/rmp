package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchTableStatusResponse;
import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import com.biplab.dholey.rmp.repositories.TableItemRepository;
import com.biplab.dholey.rmp.transformers.RestaurantTableControllerAddTableRequestToTableItemTransformer;
import com.biplab.dholey.rmp.transformers.TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.transaction.annotation.Isolation.SERIALIZABLE;

@Service
public class RestaurantTableService {

    @Autowired
    private TableItemRepository tableItemRepository;

    @Autowired
    private RestaurantTableControllerAddTableRequestToTableItemTransformer restaurantTableControllerAddTableRequestToTableItemTransformer;


    private TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer;

    public BaseDBOperationsResponse addTable(RestaurantTableControllerAddTableRequest addTableRequest) {
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
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
    public BaseDBOperationsResponse bookTable(Long tableNumber) {
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No table available for table number: "+tableNumber);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.BOOKED);
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

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse unBookTable(Long tableNumber) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.BOOKED);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("NO booked table found for table number: "+tableNumber);
                return parentResponse;
            }
            tableItem.setStatus(TableItemStatusEnum.AVAILABLE);
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

    @Transactional(isolation = SERIALIZABLE)
    public BaseDBOperationsResponse removeTable(Long tableNumber) {
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No table found for table number :"+tableNumber);
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
        RestaurantTableControllerFetchTableStatusResponse parentResponse =  new RestaurantTableControllerFetchTableStatusResponse();
        try {
            TableItem tableItem = tableItemRepository.findByTableNumberAndStatus(tableNumber, TableItemStatusEnum.AVAILABLE);
            if (tableItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Table not available for tableNumber: "+tableNumber);
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
            RestaurantTableControllerFetchAllAvailableTablesResponse response  = tableItemToRestaurantTableControllerFetchTableStatusResponseTransformer.transform(tableItemsList);
            response.setStatusCode(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

}
