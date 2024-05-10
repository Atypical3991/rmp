package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RestaurantWaiterService {

    @Autowired
    RestaurantOrderService restaurantOrderService;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public void serveReadyToServerFood() {
        Map<Long, List<OrderItem>> ordersToBeServed = restaurantOrderService.fetchReadyToServeOrders();
        if (ordersToBeServed != null) {
            log.info("Inside serveReadyToServerFood, ordersToBeServed tableIds : {}", ordersToBeServed.keySet());
            for (Map.Entry<Long, List<OrderItem>> entry : ordersToBeServed.entrySet()) {
                Long tableId = entry.getKey();
                List<OrderItem> orderItemList = entry.getValue();
                List<Long> orderIds = orderItemList.stream().map(OrderItem::getId).toList();
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (Exception e) {
                    log.error("Inside serveReadyToServerFood, ");
                }
                restaurantTableBookService.updateLastOrderServedAt(tableId);
                if (!restaurantOrderService.updateOrderStatusByListOfOrderIds(OrderItemStatusEnum.SERVED, orderIds)) {
                    throw new RuntimeException("updateOrderStatusByListOfOrderIds failed.");
                }
            }
        }

    }
}
