package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantPaymentControllerUpdatePaymentStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class RestaurantPaymentService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantPaymentService.class));
    @Autowired
    private RestaurantBillService restaurantBillService;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse initiateOrderPayment(Long billId) {
        try {
            logger.info("initiateOrderPayment called!!", "initiateOrderPayment", RestaurantPaymentService.class.toString(), Map.of("billId", billId.toString()));
            BillItem billItem = restaurantBillService.getBillGeneratedBillItemById(billId);
            if (billItem == null) {
                logger.info("billItem not found!!", "initiateOrderPayment", RestaurantPaymentService.class.toString(), Map.of("billId", billId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("Bill item not found.");
            }
            if (!restaurantBillService.updatePaymentInitiatedBillItemStatus(billItem.getId())) {
                logger.info("updatePaymentInitiatedBillItemStatus failed!!", "initiateOrderPayment", RestaurantPaymentService.class.toString(), Map.of("billId", billId.toString()));
                throw new RuntimeException("updatePaymentInitiatedBillItemStatus failed!!");
            }
            logger.info("initiateOrderPayment successfully processed.", "initiateOrderPayment", RestaurantPaymentService.class.toString(), Map.of("billId", billId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "initiateOrderPayment processed successfully.");
        } catch (Exception e) {
            logger.info("Exception raised in initiateOrderPayment.", "initiateOrderPayment", RestaurantPaymentService.class.toString(), Map.of("billId", billId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse updatePaymentStatus(RestaurantPaymentControllerUpdatePaymentStatusRequest updatePaymentStatusRequest) {
        try {
            logger.info("updatePaymentStatus called!!", "updatePaymentStatus", RestaurantPaymentService.class.toString(), Map.of("updatePaymentStatusRequest", updatePaymentStatusRequest.toString()));
            Long billId = updatePaymentStatusRequest.getBillId();
            BillItem billItem = restaurantBillService.fetchPaymentInitiatedBillItemById(billId);
            if (billItem == null) {
                logger.info("billItem not found!!", "updatePaymentStatus", RestaurantPaymentService.class.toString(), Map.of("updatePaymentStatusRequest", updatePaymentStatusRequest.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("billItem not found.");
            }
            String status = updatePaymentStatusRequest.getStatus();
            if (!restaurantBillService.updatePaymentStatus(billId, BillItemStatusEnum.valueOf(status))) {
                logger.info("updatePaymentStatus failed!!", "updatePaymentStatus", RestaurantPaymentService.class.toString(), Map.of("updatePaymentStatusRequest", updatePaymentStatusRequest.toString()));
                throw new RuntimeException("updatePaymentStatus failed!!");
            }
            if (BillItemStatusEnum.valueOf(status) == BillItemStatusEnum.PAYMENT_SUCCESS) {
                if (!restaurantTableBookService.updatePaymentReceivedAt(billItem.getTableItemId())) {
                    throw new RuntimeException("updatePaymentReceivedAt failed.");
                }
            }
            logger.info("updatePaymentStatus successfully processed!!", "updatePaymentStatus", RestaurantPaymentService.class.toString(), Map.of("updatePaymentStatusRequest", updatePaymentStatusRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "updatePaymentStatus successfully processed.");
        } catch (Exception e) {
            logger.info("Exception raised in updatePaymentStatus!!", "updatePaymentStatus", RestaurantPaymentService.class.toString(), Map.of("updatePaymentStatusRequest", updatePaymentStatusRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }
}
