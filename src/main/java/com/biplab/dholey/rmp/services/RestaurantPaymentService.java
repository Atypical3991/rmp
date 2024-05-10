package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantPaymentControllerUpdatePaymentStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RestaurantPaymentService {

    @Autowired
    private RestaurantBillService restaurantBillService;

    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse initiateOrderPayment(Long billId) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            BillItem billItem = restaurantBillService.getBillGeneratedBillItemById(billId);
            if (billItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No billItem found for billId: " + billId);
                return parentResponse;
            }
            if (!restaurantBillService.updatePaymentInitiatedBillItemStatus(billItem.getId())) {
                parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
                parentResponse.setError("Something went wrong");
                return parentResponse;
            } else {
                parentResponse.setStatusCode(HttpStatus.OK.value());
                parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
                parentResponse.getData().setSuccess(true);
                return parentResponse;
            }
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse updatePaymentStatus(RestaurantPaymentControllerUpdatePaymentStatusRequest updatePaymentStatusRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            Long billId = updatePaymentStatusRequest.getBillId();
            BillItem billItem = restaurantBillService.fetchPaymentInitiatedBillItemById(billId);
            if (billItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("No billItem found for billID: " + billId);
                return parentResponse;
            }
            String status = updatePaymentStatusRequest.getStatus();
            if (!restaurantBillService.updatePaymentStatus(billId, BillItemStatusEnum.valueOf(status))) {
                throw new RuntimeException("updatePaymentStatus failed!!");
            }
            if (BillItemStatusEnum.valueOf(status) == BillItemStatusEnum.PAYMENT_SUCCESS) {
                restaurantTableBookService.updatePaymentReceivedAt(billItem.getTableItemId());
            }
            parentResponse.setStatusCode(HttpStatus.OK.value());
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }
}
