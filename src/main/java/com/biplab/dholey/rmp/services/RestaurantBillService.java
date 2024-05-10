package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerUpdateBillStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchAllBillsByTableIdResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerOrderBillProcessingStatusResponse;
import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.GenerateBillTaskQueueModel;
import com.biplab.dholey.rmp.repositories.BillItemRepository;
import com.biplab.dholey.rmp.util.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantBillService {

    private final TaskQueue generateBillTaskQueue = new TaskQueue("restaurant_generate_bill_task_queue", 100);

    @Autowired
    private RestaurantOrderService restaurantOrderService;

    @Autowired
    private FoodMenuService foodMenuService;

    @Autowired
    private BillItemRepository billItemRepository;

    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public BillItem getBillGeneratedBillItemById(Long billId) {
        return billItemRepository.findByIdAndStatus(billId, BillItemStatusEnum.BILL_GENERATED);
    }

    public BillItem fetchPaymentInitiatedBillItemById(Long billItemId) {
        return billItemRepository.findByIdAndStatus(billItemId, BillItemStatusEnum.PAYMENT_INITIATED);
    }


    public Boolean updatePaymentInitiatedBillItemStatus(Long billId) {
        Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
        if (billItemOpt.isEmpty()) {
            return false;
        }
        billItemOpt.get().setStatus(BillItemStatusEnum.PAYMENT_INITIATED);
        billItemRepository.save(billItemOpt.get());
        return true;
    }

    public Boolean updatePaymentStatus(Long billId, BillItemStatusEnum status) {
        Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
        if (billItemOpt.isEmpty()) {
            return false;
        }
        billItemOpt.get().setStatus(status);
        billItemRepository.save(billItemOpt.get());
        return true;
    }

    @Transactional
    public void processGenerateBillTask(GenerateBillTaskQueueModel generateBillTaskQueueModel) {
        try {
            List<Long> orderItemsIds = generateBillTaskQueueModel.getOrderItemIds();
            List<OrderItem> orderItemsList = restaurantOrderService.fetchAllOrdersByOrderIds(orderItemsIds);
            BillItem billItem = new BillItem();
            for (OrderItem orderItem : orderItemsList) {
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    continue;
                }
                billItem.setPayable(billItem.getPayable() + (foodMenuItem.getPrice() * orderItem.getQuantity()));
                billItem.getOrderItemIds().add(orderItem.getId());
                orderItem.setBillGenerated(true);
                if (!restaurantOrderService.updateBillGenerationStatus(orderItem.getId())) {
                    throw new RuntimeException("Bill generation status hasn't been updated!! for tableId: " + generateBillTaskQueueModel.getTableId() + " and orderIds : " + String.join(",", generateBillTaskQueueModel.getOrderItemIds().stream().map(Object::toString).toArray(String[]::new)));
                }
            }
            billItem.setStatus(BillItemStatusEnum.BILL_GENERATED);
            billItem.setTableItemId(generateBillTaskQueueModel.getTableId());
            billItemRepository.save(billItem);
            restaurantTableBookService.updateBillGenerateAt(generateBillTaskQueueModel.getTableId());
        } catch (Exception e) {
        }


    }

    @Transactional
    public BaseDBOperationsResponse generateBill(RestaurantBillControllerGenerateBillRequest generateBillRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            List<Long> orderIds = generateBillRequest.getOrderIds();
            List<OrderItem> ordersItemList = restaurantOrderService.fetchAllUnBilledOrders(orderIds);
            if (ordersItemList.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No UnBilled Orders found!!");
                return parentResponse;
            }

            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.setMessage("Bill generation has been queued!! you'll be notified once its been processed.");
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }

    }

    public RestaurantBillControllerOrderBillProcessingStatusResponse fetchBillStatus(Long billId) {
        RestaurantBillControllerOrderBillProcessingStatusResponse parentResponse = new RestaurantBillControllerOrderBillProcessingStatusResponse();
        try {
            Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
            if (billItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty billItemOpt returned for billId: " + billId);
                return parentResponse;
            }
            parentResponse.setData(new RestaurantBillControllerOrderBillProcessingStatusResponse.RestaurantBillControllerOrderBillProcessingStatusResponseResponseData());
            parentResponse.getData().setStatus(billItemOpt.get().getStatus().name());
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse updateBillStatus(RestaurantBillControllerUpdateBillStatusRequest updateBillRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            BillItemStatusEnum status = BillItemStatusEnum.valueOf(updateBillRequest.getStatus());
            if (!List.of(BillItemStatusEnum.BILL_PROCESSED, BillItemStatusEnum.PAYMENT_INITIATED, BillItemStatusEnum.PAYMENT_FAILED, BillItemStatusEnum.PAYMENT_SUCCESS).contains(status)) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Not an acceptable status value.");
                return parentResponse;
            }
            Optional<BillItem> billItemOpt = billItemRepository.findById(updateBillRequest.getBillId());
            if (billItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty billItemOpt for billId: " + updateBillRequest.getBillId());
                return parentResponse;
            }
            BillItem billItem = billItemOpt.get();
            billItem.setStatus(status);
            billItemRepository.save(billItem);
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

    public RestaurantBillControllerFetchAllBillsByTableIdResponse fetchAllBillsByTableId(Long tableId) {
        RestaurantBillControllerFetchAllBillsByTableIdResponse parentResponse = new RestaurantBillControllerFetchAllBillsByTableIdResponse();
        try {
            List<BillItem> billItems = billItemRepository.findAllByTableItemId(tableId);
            parentResponse.setData(new RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData());
            parentResponse.getData().setBills(new ArrayList<>());
            for (BillItem billItem : billItems) {
                RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData.Bill bill = new RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData.Bill();
                bill.setBillId(billItem.getId());
                bill.setStatus(billItem.getStatus().name());
                parentResponse.getData().getBills().add(bill);
            }
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }

    }

    public GenerateBillTaskQueueModel popGenerateBillTask() {
        return (GenerateBillTaskQueueModel) generateBillTaskQueue.popTask();
    }
}
