package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RestaurantWaiterService {

    @Autowired
    RestaurantOrderService restaurantOrderService;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public void serveReadyToServerFood() {
        Map<Long, List<OrderItem>> ordersTobeServed = restaurantOrderService.fetchReadyToServeOrders();
        for (Map.Entry<Long, List<OrderItem>> entry : ordersTobeServed.entrySet()) {
            Long tableId = entry.getKey();
            List<OrderItem> orderItemList = entry.getValue();
            List<Long> orderIds = orderItemList.stream().map(OrderItem::getId).toList();
            try {
                Thread.sleep(1000 * 60 * 5);
            } catch (Exception e) {
            }
            restaurantTableBookService.updateLastOrderServedAt(tableId);
            restaurantOrderService.updateOrderStatusByListOfOrderIds(OrderItemStatusEnum.SERVED, orderIds);
        }
    }
}
