package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.KitchenStockControllerRequestRefillRequest;
import com.biplab.dholey.rmp.models.api.request.KitchenStockControllerUpdateRefillRequestStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.KitchenStockControllerCheckCurrentStockResponse;
import com.biplab.dholey.rmp.models.api.response.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse;
import com.biplab.dholey.rmp.services.KitchenStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kitchen-stock")
public class KitchenStockController {

    @Autowired
    private KitchenStockService kitchenStockService;

    @GetMapping("/current-stock")
    public ResponseEntity<KitchenStockControllerCheckCurrentStockResponse> checkCurrentStock() {
        return ResponseEntity.ok().body(kitchenStockService.checkCurrentStock());
    }

    @PostMapping("/create-refill-stock-request")
    public ResponseEntity<BaseDBOperationsResponse> requestRefillStock(@RequestBody KitchenStockControllerRequestRefillRequest refillRequestPayload) {
        return ResponseEntity.ok().body(kitchenStockService.createRefillKitchenStockRequest(refillRequestPayload));
    }

    @GetMapping("/fetch-all-refill-stock-requests")
    public ResponseEntity<KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse> fetchAllRefillRequests() {
        return ResponseEntity.ok().body(kitchenStockService.fetchAllRefillRequests());
    }

    @PutMapping("/update-refill-request-status")
    public ResponseEntity<BaseDBOperationsResponse> updateRefillStockRequestStatus(@RequestBody KitchenStockControllerUpdateRefillRequestStatusRequest updateRefillRequest) {
        return ResponseEntity.ok().body(kitchenStockService.updateRefillRequestStatus(updateRefillRequest));
    }


    @PutMapping("/delete-refill-stock-request")
    public ResponseEntity<BaseDBOperationsResponse> deleteRefillStockRequest(@RequestParam(value = "refillRequestId") Long refillRequestId) {
        return ResponseEntity.ok().body(kitchenStockService.deleteRefillStockOrderRequest(refillRequestId));
    }

}
