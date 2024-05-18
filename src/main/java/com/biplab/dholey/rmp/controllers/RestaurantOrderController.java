package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantOrderControllerCreateOrderRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerCheckOrderStatusResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerFetchAllOrdersByTableResponse;
import com.biplab.dholey.rmp.services.RestaurantOrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-order")
public class RestaurantOrderController {

    @Autowired
    private RestaurantOrderService restaurantOrderService;

    @PostMapping("/place-order")
    public ResponseEntity<BaseDBOperationsResponse> placeOrder(@Valid @RequestBody RestaurantOrderControllerCreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok().body(restaurantOrderService.placeOrder(createOrderRequest));
    }

    @GetMapping("/fetch-order-details-by-id")
    public ResponseEntity<RestaurantOrderControllerCheckOrderStatusResponse> fetchOrderDetailsById(@Valid @Positive(message = "orderId should be greater than 0.") @RequestParam(value = "orderId") Long orderId) {
        return ResponseEntity.ok().body(restaurantOrderService.getOrderDetails(orderId));
    }

    @PutMapping("/cancel-order")
    public ResponseEntity<BaseDBOperationsResponse> cancelOrder(@Valid @Positive(message = "orderId should ne greater than 0.") @RequestParam(value = "orderId") Long orderId) {
        return ResponseEntity.ok().body(restaurantOrderService.cancelOrder(orderId));
    }

    @GetMapping("/fetch-all-orders-by-table-id")
    public ResponseEntity<RestaurantOrderControllerFetchAllOrdersByTableResponse> fetchAllOrdersByTableId(@Valid @Positive(message = "tableId should be greater than 0.") @RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantOrderService.fetchAllOrdersByTableId(tableId));
    }
}
