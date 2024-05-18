package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantPaymentControllerUpdatePaymentStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.services.RestaurantPaymentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-payment")
public class RestaurantPaymentController {

    @Autowired
    private RestaurantPaymentService restaurantPaymentService;

    @PostMapping("/initiate-payment")
    public ResponseEntity<BaseDBOperationsResponse> initiatePayment(@Valid @Positive(message = "billId should be greater than 0.") @RequestParam(value = "billId") Long billId) {
        return ResponseEntity.ok().body(restaurantPaymentService.initiateOrderPayment(billId));
    }

    @PostMapping("/update-payment-status")
    public ResponseEntity<BaseDBOperationsResponse> updatePaymentStatus(@Valid @RequestBody RestaurantPaymentControllerUpdatePaymentStatusRequest updatePaymentStatusRequest) {
        return ResponseEntity.ok().body(restaurantPaymentService.updatePaymentStatus(updatePaymentStatusRequest));
    }
}
