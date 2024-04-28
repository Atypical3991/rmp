package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerUpdateBillStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerCreateAndProcessOrderBillResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchAllBillsByTableIdResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerOrderBillProcessingStatusResponse;
import com.biplab.dholey.rmp.services.RestaurantBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-bill")
public class RestaurantBillController {

    @Autowired
    private RestaurantBillService restaurantBillService;

    @PostMapping("/generate-bill")
    public ResponseEntity<BaseDBOperationsResponse> generateOrderBill(@RequestBody RestaurantBillControllerGenerateBillRequest generateBillRequest) {
        return ResponseEntity.ok().body(restaurantBillService.generateBill(generateBillRequest));
    }

    @GetMapping("/bill-status")
    public ResponseEntity<RestaurantBillControllerOrderBillProcessingStatusResponse> orderBillProcessingStatus(@RequestParam(value = "billId") Long billId) {
        return ResponseEntity.ok().body(restaurantBillService.fetchBillStatus(billId));
    }

    @PutMapping("/update-bill-status")
    public  ResponseEntity<BaseDBOperationsResponse> updateBillStatus(@RequestBody RestaurantBillControllerUpdateBillStatusRequest updateBillStatusRequest) {
        return ResponseEntity.ok().body(restaurantBillService.updateBillStatus(updateBillStatusRequest));
    }

    @GetMapping("/fetch-all-bills-by-table-id")
    public ResponseEntity<RestaurantBillControllerFetchAllBillsByTableIdResponse> fetchAllBillsByTableId(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantBillService.fetchAllBillsByTableId(tableId));
    }
}
