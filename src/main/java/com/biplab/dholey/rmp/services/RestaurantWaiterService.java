package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RestaurantWaiterService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantWaiterService.class));
    @Autowired
    RestaurantOrderService restaurantOrderService;

    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public void serveReadyToServerFood() {
//        logger.info("serveReadyToServerFood called!!", "serveReadyToServerFood", RestaurantWaiterService.class.toString(), null);
        Map<Long, List<OrderItem>> ordersToBeServed = restaurantOrderService.fetchReadyToServeOrders();
        if (ordersToBeServed != null) {
            for (Map.Entry<Long, List<OrderItem>> entry : ordersToBeServed.entrySet()) {
                Long tableId = entry.getKey();
                List<OrderItem> orderItemList = entry.getValue();
                List<Long> orderIds = orderItemList.stream().map(OrderItem::getId).toList();
                try {
                    Thread.sleep(1000 * 60 * 5);
                } catch (Exception e) {
                    logger.info("Sleep method failed!!", "serveReadyToServerFood", RestaurantWaiterService.class.toString(), null);
                }
                if (!restaurantTableBookService.updateLastOrderServedAt(tableId)) {
                    logger.error("updateLastOrderServedAt failed!!", "serveReadyToServerFood", RestaurantWaiterService.class.toString(), new RuntimeException("updateLastOrderServedAt failed!!"), Map.of("tableId", tableId.toString()));
                    throw new RuntimeException("updateLastOrderServedAt failed.");
                }
                if (!restaurantOrderService.updateOrderStatusByListOfOrderIds(OrderItemStatusEnum.SERVED, orderIds)) {
                    logger.error("updateOrderStatusByListOfOrderIds failed!!", "serveReadyToServerFood", RestaurantWaiterService.class.toString(), new RuntimeException("updateOrderStatusByListOfOrderIds failed!!"), Map.of("tableId", tableId.toString()));
                    throw new RuntimeException("updateOrderStatusByListOfOrderIds failed.");
                }
            }
        }

    }
}
