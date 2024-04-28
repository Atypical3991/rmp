package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantOrderControllerCreateOrderRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerCheckOrderStatusResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerFetchAllOrdersByTableResponse;
import com.biplab.dholey.rmp.services.RestaurantOrderService;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-order")
public class RestaurantOrderController {

    @Autowired
    private RestaurantOrderService restaurantOrderService;

    @PostMapping("/create-order")
    public ResponseEntity<BaseDBOperationsResponse> createOrder(@RequestBody RestaurantOrderControllerCreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok().body(restaurantOrderService.createOrder(createOrderRequest));
    }

    @GetMapping("/order-status")
    public ResponseEntity<RestaurantOrderControllerCheckOrderStatusResponse> checkOrderStatus(@RequestParam(value = "orderId") Long orderId) {
        return ResponseEntity.ok().body(restaurantOrderService.getOrderStatus(orderId));
    }

    @PutMapping("/delete-order")
    public ResponseEntity<BaseDBOperationsResponse> deleteOrder(@RequestParam(value = "orderId") Long orderId) {
        return ResponseEntity.ok().body(restaurantOrderService.deleteOrder(orderId));
    }

    @GetMapping("fetch-active-orders-by-table-id")
    public ResponseEntity<RestaurantOrderControllerFetchAllOrdersByTableResponse> fetchAllActiveOrdersByTableId(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantOrderService.fetchAllActiveOrdersByTableId(tableId));
    }
}
