package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchAllBillsByTableIdResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchBillDetailsByBillIdResponse;
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
    private RestaurantFoodMenuService restaurantFoodMenuService;
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
            Optional<BillItem> billItemOpt = billItemRepository.findById(generateBillTaskQueueModel.getBillItemId());
            if (billItemOpt.isEmpty()) {
                throw new RuntimeException("No Bill item found!!");
            }
            BillItem billItem = billItemOpt.get();
            List<Long> orderItemsIds = billItem.getOrderItemIds();
            List<OrderItem> orderItemsList = restaurantOrderService.fetchAllOrdersByOrderIds(orderItemsIds);

            for (OrderItem orderItem : orderItemsList) {
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.error("foodMenuItem not found!!", "processGenerateBillTask", RestaurantBillService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException("foodMenuItem not found!!");
                }
                billItem.setPayable(billItem.getPayable() + (foodMenuItem.getPrice() * orderItem.getQuantity()));
                billItem.getOrderItemIds().add(orderItem.getId());
                if (!restaurantOrderService.updateBillGenerationStatus(orderItem.getId())) {
                    logger.info("updateBillGenerationStatus failed!!", "processGenerateBillTask", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException("updateBillGenerationStatus call failed!!");
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
            List<OrderItem> unBilledOrdersItemList = restaurantOrderService.fetchAllUnBilledOrders(orderIds);
            if (unBilledOrdersItemList.isEmpty()) {
                logger.info("No UnBilled Orders found!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("No UnBilled Orders found!!");
            }

            BillItem billItem = new BillItem();
            for (OrderItem orderItem : unBilledOrdersItemList) {
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.error("foodMenuItem not found!!", "generateBill", RestaurantBillService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException("foodMenuItem not found!!");
                }
                billItem.setPayable(billItem.getPayable() + (foodMenuItem.getPrice() * orderItem.getQuantity()));
                billItem.getOrderItemIds().add(orderItem.getId());
                if (!restaurantOrderService.updateBillGenerationStatus(orderItem.getId())) {
                    logger.info("updateBillGenerationStatus failed!!", "generateBill", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException("");
                }
            }
            billItem.setStatus(BillItemStatusEnum.BILL_GENERATION_QUEUED);
            billItem.setTableItemId(generateBillRequest.getTableItemId());
            billItemRepository.save(billItem);

            try {
                if (!generateBillCustomTaskQueue.pushTask(new GenerateBillTaskQueueModel(billItem.getId(), generateBillRequest.getTableItemId()))) {
                    logger.error("generateBillCustomTaskQueue push task failed!!", "generateBill", RestaurantBillService.class.toString(), new RuntimeException("generateBillCustomTaskQueue push task failed!!"), Map.of("generateBillRequest", generateBillRequest.toString()));
                }
            } catch (Exception e) {
                logger.error("Exception raised while pushing Generate Bill task!!", "generateBill", RestaurantBillService.class.toString(), e, Map.of("generateBillRequest", generateBillRequest.toString()));
            }

            logger.info("generateBill successfully processed!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Bill generation has been queued!! you'll be notified once its been processed.");
        } catch (Exception e) {
            logger.error("Exception raised in generateBill!!", "generateBill", RestaurantBillService.class.toString(), e, Map.of("generateBillRequest", generateBillRequest.toString()));
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

    public RestaurantBillControllerFetchBillDetailsByBillIdResponse fetchBillDetailsById(Long billId) {
        try {
            logger.info("fetchBillDetailsById called!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
            if (billItemOpt.isEmpty()) {
                new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getNotFoundServerErrorResponse("BillItem not found!!");
            }

            RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData data = new RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData();
            BillItem billItem = billItemOpt.get();
            data.setTableNumber(billItem.getTableItemId());
            data.setTotalPayable(billItem.getPayable());
            if (billItem.getStatus() == BillItemStatusEnum.PAYMENT_SUCCESS) {
                data.setPaymentStatus("Payment Successfully Received");
            } else if (billItem.getStatus() == BillItemStatusEnum.PAYMENT_FAILED) {
                data.setPaymentStatus("Last Payment Failed.");
            } else {
                data.setPaymentStatus("Payment hasn't been generated");
            }
            data.setOrdersList(new ArrayList<>());
            List<RestaurantBillControllerFetchBillDetailsByBillIdResponse.OrderDetails> orderDetailsListResponse = data.getOrdersList();
            List<OrderItem> orderItemList = restaurantOrderService.fetchAllOrdersByOrderIds(billItem.getOrderItemIds());
            for (OrderItem orderItem : orderItemList) {
                RestaurantBillControllerFetchBillDetailsByBillIdResponse.OrderDetails orderDetails = new RestaurantBillControllerFetchBillDetailsByBillIdResponse.OrderDetails();
                orderDetails.setOrderId(orderItem.getId());
                orderDetails.setTotalPrice(orderItem.getTotalPrice());

                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.error("FoodMenuItem not found!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("billId", billId.toString(), "foodMenuItemId", orderItem.getFoodMenuItemId().toString()));
                    throw new RuntimeException("FoodMenuItem not found!!");
                }
                orderDetails.setFoodItemName(foodMenuItem.getName());
                orderDetailsListResponse.add(orderDetails);
            }

            logger.info("fetchBillDetailsById successfully processed!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            return new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getSuccessResponse(data, "fetchBillDetailsById successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in fetchBillDetailsById", "fetchBillDetailsById", RestaurantBillService.class.toString(), e, Map.of("billId", billId.toString()));
            return new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getInternalServerErrorResponse("Internal server error.", e);
        }

    }

}
