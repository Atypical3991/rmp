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
import com.biplab.dholey.rmp.util.CustomLogger;
import com.biplab.dholey.rmp.util.CustomTaskQueue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RestaurantBillService {

    private final CustomTaskQueue generateBillCustomTaskQueue = new CustomTaskQueue("restaurant_generate_bill_task_queue", 100);
    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantBillService.class));
    @Autowired
    private RestaurantOrderService restaurantOrderService;
    @Autowired
    private FoodMenuService foodMenuService;
    @Autowired
    private BillItemRepository billItemRepository;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public BillItem getBillGeneratedBillItemById(Long billId) {
        logger.info("getBillGeneratedBillItemById called!!", "getBillGeneratedBillItemById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
        return billItemRepository.findByIdAndStatus(billId, BillItemStatusEnum.BILL_GENERATED);
    }

    public BillItem fetchPaymentInitiatedBillItemById(Long billId) {
        logger.info("fetchPaymentInitiatedBillItemById called!!", "fetchPaymentInitiatedBillItemById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
        return billItemRepository.findByIdAndStatus(billId, BillItemStatusEnum.PAYMENT_INITIATED);
    }


    public Boolean updatePaymentInitiatedBillItemStatus(Long billId) {
        logger.info("updatePaymentInitiatedBillItemStatus called!!", "updatePaymentInitiatedBillItemStatus", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
        Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
        if (billItemOpt.isEmpty()) {
            logger.error("updatePaymentInitiatedBillItemStatus called!!", "updatePaymentInitiatedBillItemStatus", RestaurantBillService.class.toString(), new RuntimeException("Bill not found"), Map.of("billId", billId.toString()));
            return false;
        }
        billItemOpt.get().setStatus(BillItemStatusEnum.PAYMENT_INITIATED);
        billItemRepository.save(billItemOpt.get());
        return true;
    }

    public Boolean updatePaymentStatus(Long billId, BillItemStatusEnum status) {
        logger.info("updatePaymentStatus called!!", "updatePaymentStatus", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
        Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
        if (billItemOpt.isEmpty()) {
            logger.error("Bill item not found!!", "updatePaymentStatus", RestaurantBillService.class.toString(), new RuntimeException("Bill item not found."), Map.of("billId", billId.toString()));
            return false;
        }
        billItemOpt.get().setStatus(status);
        billItemRepository.save(billItemOpt.get());
        return true;
    }

    public GenerateBillTaskQueueModel popGenerateBillTask() {
        logger.info("popGenerateBillTask called!!", "updatePaymentStatus", RestaurantBillService.class.toString(), null);
        return (GenerateBillTaskQueueModel) generateBillCustomTaskQueue.popTask();
    }

    @Transactional
    public void processGenerateBillTask(GenerateBillTaskQueueModel generateBillTaskQueueModel) {
        try {
            logger.info("processGenerateBillTask called!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("generateBillTaskQueueModel", generateBillTaskQueueModel.toString()));
            List<Long> orderItemsIds = generateBillTaskQueueModel.getOrderItemIds();
            List<OrderItem> orderItemsList = restaurantOrderService.fetchAllOrdersByOrderIds(orderItemsIds);
            BillItem billItem = new BillItem();
            for (OrderItem orderItem : orderItemsList) {
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.info("foodMenuItem not found!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    continue;
                }
                billItem.setPayable(billItem.getPayable() + (foodMenuItem.getPrice() * orderItem.getQuantity()));
                billItem.getOrderItemIds().add(orderItem.getId());
                orderItem.setBillGenerated(true);
                if (!restaurantOrderService.updateBillGenerationStatus(orderItem.getId())) {
                    logger.info("updateBillGenerationStatus failed!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException("Bill generation status hasn't been updated!! for tableId: " + generateBillTaskQueueModel.getTableId() + " and orderIds : " + String.join(",", generateBillTaskQueueModel.getOrderItemIds().stream().map(Object::toString).toArray(String[]::new)));
                }
            }
            billItem.setStatus(BillItemStatusEnum.BILL_GENERATED);
            billItem.setTableItemId(generateBillTaskQueueModel.getTableId());
            billItemRepository.save(billItem);
            if (!restaurantTableBookService.updateBillGenerateAt(generateBillTaskQueueModel.getTableId())) {
                throw new RuntimeException("updateBillGenerateAt call failed!!");
            }
            logger.info("processGenerateBillTask successfully resolved!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("generateBillTaskQueueModel", generateBillTaskQueueModel.toString()));
        } catch (Exception e) {
            logger.info("Exception raised in processGenerateBillTask!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("generateBillTaskQueueModel", generateBillTaskQueueModel.toString()));
        }
    }

    @Transactional
    public BaseDBOperationsResponse generateBill(RestaurantBillControllerGenerateBillRequest generateBillRequest) {
        try {
            logger.info("generateBill called!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
            List<Long> orderIds = generateBillRequest.getOrderIds();
            List<OrderItem> ordersItemList = restaurantOrderService.fetchAllUnBilledOrders(orderIds);
            if (ordersItemList.isEmpty()) {
                logger.info("No UnBilled Orders found!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("No UnBilled Orders found!!");
            }
            logger.info("generateBill successfully processed!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Bill generation has been queued!! you'll be notified once its been processed.");
        } catch (Exception e) {
            logger.error("Exception raised in generateBill!!", "generateBill", RestaurantBillService.class.toString(), e, Map.of("generateBillRequest", generateBillRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }

    }

    public RestaurantBillControllerOrderBillProcessingStatusResponse fetchBillStatus(Long billId) {
        try {
            logger.info("fetchBillStatus called!!", "fetchBillStatus", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
            if (billItemOpt.isEmpty()) {
                logger.info("No bill item found.", "fetchBillStatus", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
                return new RestaurantBillControllerOrderBillProcessingStatusResponse().getNotFoundServerErrorResponse("Bill item not found");
            }
            RestaurantBillControllerOrderBillProcessingStatusResponse.RestaurantBillControllerOrderBillProcessingStatusResponseResponseData data = new RestaurantBillControllerOrderBillProcessingStatusResponse.RestaurantBillControllerOrderBillProcessingStatusResponseResponseData();
            data.setStatus(billItemOpt.get().getStatus().name());
            logger.info("fetchBillStatus successfully processed!!", "fetchBillStatus", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            return new RestaurantBillControllerOrderBillProcessingStatusResponse().getSuccessResponse(data, "fetchBillStatus successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in fetchBillStatus", "fetchBillStatus", RestaurantBillService.class.toString(), e, Map.of("billId", billId.toString()));
            return new RestaurantBillControllerOrderBillProcessingStatusResponse().getInternalServerErrorResponse("Internal server error.", e);
        }
    }

    public BaseDBOperationsResponse updateBillStatus(RestaurantBillControllerUpdateBillStatusRequest updateBillRequest) {
        try {
            logger.info("updateBillStatus called!!", "updateBillStatus", RestaurantBillService.class.toString(), Map.of("updateBillRequest", updateBillRequest.toString()));
            BillItemStatusEnum status = BillItemStatusEnum.valueOf(updateBillRequest.getStatus());
            if (!List.of(BillItemStatusEnum.BILL_PROCESSED, BillItemStatusEnum.PAYMENT_INITIATED, BillItemStatusEnum.PAYMENT_FAILED, BillItemStatusEnum.PAYMENT_SUCCESS).contains(status)) {
                logger.info("un-supported status received!!", "updateBillStatus", RestaurantBillService.class.toString(), Map.of("updateBillRequest", updateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("un-supported status received!!");
            }
            Optional<BillItem> billItemOpt = billItemRepository.findById(updateBillRequest.getBillId());
            if (billItemOpt.isEmpty()) {
                logger.info("bill item not found!!", "updateBillStatus", RestaurantBillService.class.toString(), Map.of("updateBillRequest", updateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("bill item not found!!");
            }
            BillItem billItem = billItemOpt.get();
            billItem.setStatus(status);
            billItemRepository.save(billItem);
            logger.info("updateBillStatus successfully processed!!", "updateBillStatus", RestaurantBillService.class.toString(), Map.of("updateBillRequest", updateBillRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "updateBillStatus successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in updateBillStatus", "updateBillStatus", RestaurantBillService.class.toString(), e, Map.of("updateBillRequest", updateBillRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public RestaurantBillControllerFetchAllBillsByTableIdResponse fetchAllBillsByTableId(Long tableId) {
        try {
            logger.info("fetchAllBillsByTableId called!!", "fetchAllBillsByTableId", RestaurantBillService.class.toString(), Map.of("tableId", tableId.toString()));
            List<BillItem> billItems = billItemRepository.findAllByTableItemId(tableId);
            RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData data = new RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData();
            data.setBills(new ArrayList<>());
            for (BillItem billItem : billItems) {
                RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData.Bill bill = new RestaurantBillControllerFetchAllBillsByTableIdResponse.RestaurantBillControllerFetchAllBillsByTableIdResponseResponseData.Bill();
                bill.setBillId(billItem.getId());
                bill.setStatus(billItem.getStatus().name());
                data.getBills().add(bill);
            }
            logger.info("fetchAllBillsByTableId successfully processed!!", "fetchAllBillsByTableId", RestaurantBillService.class.toString(), Map.of("tableId", tableId.toString()));
            return new RestaurantBillControllerFetchAllBillsByTableIdResponse().getSuccessResponse(data, "fetchAllBillsByTableId successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllBillsByTableId", "fetchAllBillsByTableId", RestaurantBillService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new RestaurantBillControllerFetchAllBillsByTableIdResponse().getInternalServerErrorResponse("Internal server error.", e);
        }

    }

}
