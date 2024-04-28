package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantPaymentControllerUpdatePaymentStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantPaymentControllerCheckOrderBillPaymentStatusResponse;
import com.biplab.dholey.rmp.repositories.OrderItemRepository;
import com.biplab.dholey.rmp.services.RestaurantPaymentService;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-payment")
public class RestaurantPaymentController {

    @Autowired
    private RestaurantPaymentService restaurantPaymentService;

    @PostMapping("/initiate-payment")
    public ResponseEntity<BaseDBOperationsResponse> initiatePayment(@RequestParam(value = "billId") Long billId) {
        return ResponseEntity.ok().body(restaurantPaymentService.initiateOrderPayment(billId));
    }

    @GetMapping("/update-payment-status")
    public ResponseEntity<BaseDBOperationsResponse> updatePaymentStatus(@RequestBody RestaurantPaymentControllerUpdatePaymentStatusRequest updatePaymentStatusRequest) {
        return ResponseEntity.ok().body(restaurantPaymentService.updatePaymentStatus(updatePaymentStatusRequest));
    }
}
