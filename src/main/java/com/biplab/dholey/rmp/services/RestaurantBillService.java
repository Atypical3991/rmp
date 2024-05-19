package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchAllBillsByTableIdResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchBillDetailsByBillIdResponse;
import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.biplab.dholey.rmp.repositories.BillItemRepository;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.biplab.dholey.rmp.common.CustomError.CommonErrors.INTERNAL_SERVER_ERROR;
import static com.biplab.dholey.rmp.common.CustomError.RestaurantBillGenerationServiceErrors.*;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantBillService.*;
import static com.biplab.dholey.rmp.common.constant.EnumToTextMap.BILL_STATUS_TO_TEXT_MAP;

@Service
public class RestaurantBillService {

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

    @Transactional
    public BaseDBOperationsResponse generateBill(RestaurantBillControllerGenerateBillRequest generateBillRequest) {
        try {
            logger.info("generateBill called!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
            List<Long> orderIds = generateBillRequest.getOrderIds();
            if (orderIds.isEmpty()) {
                logger.info(GENERATE_BILL_EMPTY_ORDERS_LIST_IN_REQUEST_ERROR, "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse(GENERATE_BILL_EMPTY_ORDERS_LIST_IN_REQUEST_ERROR);
            }
            List<OrderItem> unBilledOrdersItemList = restaurantOrderService.fetchAllUnBilledOrders(orderIds);
            if (unBilledOrdersItemList.isEmpty()) {
                logger.info(GENERATE_BILL_NO_UN_BILLED_ORDER_FOUND_ERROR, "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse(GENERATE_BILL_NO_UN_BILLED_ORDER_FOUND_ERROR);
            }

            BillItem billItem = new BillItem();
            for (OrderItem orderItem : unBilledOrdersItemList) {
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.info(GENERATE_BILL_FOOD_MENU_ITEM_NOT_FOUND_ERROR, "generateBill", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException(GENERATE_BILL_FOOD_MENU_ITEM_NOT_FOUND_ERROR);
                }
                billItem.setPayable(billItem.getPayable() + (foodMenuItem.getPrice() * orderItem.getQuantity()));
                billItem.getOrderItemIds().add(orderItem.getId());
                if (!restaurantOrderService.updateBillGenerationStatus(orderItem.getId())) {
                    logger.info(GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR, "generateBill", RestaurantBillService.class.toString(), Map.of("orderItem", orderItem.toString()));
                    throw new RuntimeException(GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR);
                }
            }

            if (!restaurantTableBookService.updateBillGenerateAt(generateBillRequest.getTableItemId())) {
                throw new RuntimeException(GENERATE_UPDATE_BILL_GENERATED_AT_FAILED_ERROR);
            }
            billItem.setStatus(BillItemStatusEnum.BILL_GENERATED);
            billItem.setTableItemId(generateBillRequest.getTableItemId());
            billItemRepository.save(billItem);

            logger.info("generateBill successfully processed!!", "generateBill", RestaurantBillService.class.toString(), Map.of("generateBillRequest", generateBillRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), GENERATE_BILL_SUCCESS_RESPONSE);
        } catch (Exception e) {
            logger.error("Exception raised in generateBill!!", "generateBill", RestaurantBillService.class.toString(), e, Map.of("generateBillRequest", generateBillRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse(INTERNAL_SERVER_ERROR, e);
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
            return new RestaurantBillControllerFetchAllBillsByTableIdResponse().getSuccessResponse(data, FETCH_ALL_BILLS_BY_TABLE_ID);
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllBillsByTableId", "fetchAllBillsByTableId", RestaurantBillService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new RestaurantBillControllerFetchAllBillsByTableIdResponse().getInternalServerErrorResponse(INTERNAL_SERVER_ERROR, e);
        }

    }

    public RestaurantBillControllerFetchBillDetailsByBillIdResponse fetchBillDetailsById(Long billId) {
        try {
            logger.info("fetchBillDetailsById called!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            Optional<BillItem> billItemOpt = billItemRepository.findById(billId);
            if (billItemOpt.isEmpty()) {
                return new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getNotFoundServerErrorResponse(FETCH_BILL_DETAILS_BY_ID_BILL_ITEM_NOT_FOUND);
            }

            RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData data = new RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData();
            BillItem billItem = billItemOpt.get();
            data.setTableNumber(billItem.getTableItemId());
            data.setTotalPayable(billItem.getPayable());

            if (BILL_STATUS_TO_TEXT_MAP.get(billItem.getStatus()) != null) {
                data.setPaymentStatus(BILL_STATUS_TO_TEXT_MAP.get(billItem.getStatus()));
            } else {
                throw new RuntimeException(FETCH_BILL_DETAILS_BY_ID_RECEIVED_UNKNOWN_BILL_STATUS);
            }
            data.setOrdersList(new ArrayList<>());
            List<RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData.OrderDetails> orderDetailsListResponse = data.getOrdersList();
            List<OrderItem> orderItemList = restaurantOrderService.fetchAllOrdersByOrderIds(billItem.getOrderItemIds());
            for (OrderItem orderItem : orderItemList) {
                RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData.OrderDetails orderDetails = new RestaurantBillControllerFetchBillDetailsByBillIdResponse.RestaurantBillControllerFetchBillDetailsByBillIdResponseData.OrderDetails();
                orderDetails.setOrderId(orderItem.getId());
                orderDetails.setTotalPrice(orderItem.getTotalPrice());
                data.setTotalPayable(data.getTotalPayable() + orderItem.getTotalPrice());
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    logger.error("FoodMenuItem not found!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("billId", billId.toString(), "foodMenuItemId", orderItem.getFoodMenuItemId().toString()));
                    throw new RuntimeException(FETCH_BILL_DETAILS_BY_ID_FOOD_MENU_ITEM_NOT_FOUND);
                }
                orderDetails.setFoodItemName(foodMenuItem.getName());
                orderDetailsListResponse.add(orderDetails);
            }

            logger.info("fetchBillDetailsById successfully processed!!", "fetchBillDetailsById", RestaurantBillService.class.toString(), Map.of("billId", billId.toString()));
            return new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getSuccessResponse(data, FETCH_BILL_DETAILS_BY_ID_SUCCESS_RESPONSE);
        } catch (Exception e) {
            logger.error("Exception raised in fetchBillDetailsById", "fetchBillDetailsById", RestaurantBillService.class.toString(), e, Map.of("billId", billId.toString()));
            return new RestaurantBillControllerFetchBillDetailsByBillIdResponse().getInternalServerErrorResponse(INTERNAL_SERVER_ERROR, e);
        }

    }

}
