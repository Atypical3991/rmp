package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantOrderControllerCreateOrderRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerCheckOrderStatusResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerFetchAllOrdersByTableResponse;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.TableBookedItem;
import com.biplab.dholey.rmp.models.db.enums.BookedTableStatusEnum;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.models.util.TaskQueueModels.PrepareFoodTaskQueueModel;
import com.biplab.dholey.rmp.repositories.OrderItemRepository;
import com.biplab.dholey.rmp.util.CustomLogger;
import com.biplab.dholey.rmp.util.CustomTaskQueue;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestaurantOrderService {

    private static final Long MAX_ORDER_QUEUED_COUNT = 50L;
    private static final Long MAX_ORDER_PER_REQUEST = 10L;
    private final CustomTaskQueue prepareFoodCustomTaskQueue = new CustomTaskQueue("prepare_food_task_queue", 100);
    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantOrderService.class));
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private RestaurantFoodMenuService restaurantFoodMenuService;
    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public List<OrderItem> fetchAllOrdersByOrderIds(List<Long> orderIds) {
        logger.info("fetchAllOrdersByOrderIds called!!", "fetchAllOrdersByOrderIds", RestaurantOrderService.class.toString(), Map.of("orderIds", orderIds.toString()));
        return orderItemRepository.findAllByIdIn(orderIds);
    }

    public List<OrderItem> fetchAllUnBilledOrders(List<Long> orderIds) {
        logger.info("fetchAllUnBilledOrders called!!", "fetchAllUnBilledOrders", RestaurantOrderService.class.toString(), Map.of("orderIds", orderIds.toString()));
        return orderItemRepository.findAllByIdInAndBillGenerated(orderIds, false);
    }

    public Boolean updateOrderStatus(Long orderItemId, OrderItemStatusEnum statusEnum) {
        try {
            logger.info("updateOrderStatus called!!", "updateOrderStatus", RestaurantOrderService.class.toString(), Map.of("orderItemId", orderItemId.toString(), "statusEnum", statusEnum.toString()));
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
            if (orderItemOpt.isEmpty()) {
                logger.error("Order Item not found!!", "updateOrderStatus", RestaurantOrderService.class.toString(), new RuntimeException("Order item not found"), Map.of("orderItemId", orderItemId.toString(), "statusEnum", statusEnum.toString()));
                return false;
            }
            orderItemOpt.get().setStatus(statusEnum);
            orderItemRepository.save(orderItemOpt.get());
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateOrderStatus!!", "updateOrderStatus", RestaurantOrderService.class.toString(), e, Map.of("orderItemId", orderItemId.toString(), "statusEnum", statusEnum.toString()));
            return false;
        }
    }

    public OrderItem fetchOrderById(Long orderItemId) {
        logger.info("fetchOrderById called!!", "fetchOrderById", RestaurantOrderService.class.toString(), Map.of("orderItemId", orderItemId.toString()));
        Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
        return orderItemOpt.orElse(null);
    }


    public Map<Long, List<OrderItem>> fetchReadyToServeOrders() {
        try {
            logger.info("fetchReadyToServeOrders called!!", "fetchReadyToServeOrders", RestaurantOrderService.class.toString(), null);
            List<OrderItem> orderItemList = orderItemRepository.findAllByStatusIn(List.of(OrderItemStatusEnum.READY_TO_SERVE));
            return orderItemList.stream().collect(Collectors.groupingBy(OrderItem::getTableItemId));
        } catch (Exception e) {
            logger.error("Exception raised in fetchReadyToServeOrders!!", "fetchReadyToServeOrders", RestaurantOrderService.class.toString(), e, null);
            return null;
        }
    }

    public Boolean updateOrderStatusByListOfOrderIds(OrderItemStatusEnum statusEnum, List<Long> orderIds) {
        try {
            logger.info("updateOrderStatusByListOfOrderIds called!!", "updateOrderStatusByListOfOrderIds", RestaurantOrderService.class.toString(), Map.of("orderIds", orderIds.toString()));
            orderItemRepository.updateOrderStatusByOrderIdsDBQuery(statusEnum, LocalDateTime.now(), orderIds);
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateOrderStatusByListOfOrderIds!!", "updateOrderStatusByListOfOrderIds", RestaurantOrderService.class.toString(), e, Map.of("orderIds", orderIds.toString()));
            return false;
        }

    }

    public PrepareFoodTaskQueueModel popQueuedPrepareFoodTasks() {
        logger.info("popQueuedPrepareFoodTasks called!!", "popQueuedPrepareFoodTasks", RestaurantOrderService.class.toString(), null);
        return (PrepareFoodTaskQueueModel) prepareFoodCustomTaskQueue.popTask();
    }

    public boolean updateBillGenerationStatus(Long orderId) {
        try {
            logger.info("updateBillGenerationStatus called!!", "updateBillGenerationStatus", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                logger.error("Order item not found.", "updateBillGenerationStatus", RestaurantOrderService.class.toString(), new RuntimeException("Order item not found"), Map.of("orderId", orderId.toString()));
                return false;
            }
            orderItemOpt.get().setStatus(OrderItemStatusEnum.BILL_GENERATED);
            orderItemOpt.get().setBillGenerated(true);
            orderItemRepository.save(orderItemOpt.get());
            return true;
        } catch (Exception e) {
            logger.error("Exception raised in updateBillGenerationStatus.", "updateBillGenerationStatus", RestaurantOrderService.class.toString(), e, Map.of("orderId", orderId.toString()));
            return false;
        }

    }

    @Transactional
    public BaseDBOperationsResponse createOrder(RestaurantOrderControllerCreateOrderRequest orderRequest) {
        try {
            logger.info("createOrder called!!", "createOrder", RestaurantOrderService.class.toString(), Map.of("orderRequest", orderRequest.toString()));
            Long queuedOrdersCount = orderItemRepository.countByStatus(OrderItemStatusEnum.QUEUED);
            if (queuedOrdersCount >= MAX_ORDER_QUEUED_COUNT) {
                logger.info("queuedOrdersCount reached max threshold!!", "createOrder", RestaurantOrderService.class.toString(), Map.of("orderRequest", orderRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Can't take anymore orders.");
            }

            if (orderRequest.getOrderList().size() > MAX_ORDER_PER_REQUEST) {
                logger.info("orders per request reached max threshold!!", "createOrder", RestaurantOrderService.class.toString(), Map.of("orderRequest", orderRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Can't take more than " + MAX_ORDER_PER_REQUEST + " orders.");
            }


            Long tableId = orderRequest.getTableId();
            TableBookedItem tableBookedItem = restaurantTableBookService.getTableBookedItemByTableId(tableId);
            if (tableBookedItem == null) {
                logger.error("tableBookedItem not found!!", "createOrder", RestaurantOrderService.class.toString(), new RuntimeException("tableBookedItem not found."), Map.of("orderRequest", orderRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("TableBookItem not  found for tableId: " + tableId);
            }
            if (!Objects.equals(BookedTableStatusEnum.BOOKED, tableBookedItem.getStatus())) {
                logger.error("Table not in BOOKED state!!", "createOrder", RestaurantOrderService.class.toString(), new RuntimeException("Table not in BOOKED state"), Map.of("orderRequest", orderRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Table is not in BOOKED state for tableId: " + tableId);
            }

            List<Long> orderIds = new ArrayList<>();
            for (RestaurantOrderControllerCreateOrderRequest.Order order : orderRequest.getOrderList()) {
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(order.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("foodMenuItem not found for foodMenuItemId: " + order.getFoodMenuItemId());
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setFoodMenuItemId(order.getFoodMenuItemId());
                orderItem.setQuantity(order.getQuantity());
                orderItem.setTableItemId(tableId);
                orderItem.setStatus(OrderItemStatusEnum.QUEUED);
                orderItem.setOrderPlacedAt(LocalDateTime.now());
                orderItem.setTotalPrice(order.getQuantity() * foodMenuItem.getPrice());
                orderItemRepository.save(orderItem);
                orderIds.add(orderItem.getId());

                try {
                    if (!prepareFoodCustomTaskQueue.pushTask(new PrepareFoodTaskQueueModel(orderItem.getId(), orderItem.getTableItemId()))) {
                        logger.error("prepareFoodCustomTaskQueue task push failed!!", "createOrder", RestaurantOrderService.class.toString(), new RuntimeException("prepareFoodCustomTaskQueue task push failed!!"), Map.of("orderRequest", orderRequest.toString()));
                    }
                } catch (Exception e) {
                    logger.error("prepareFoodCustomTaskQueue task push failed!!", "createOrder", RestaurantOrderService.class.toString(), e, Map.of("orderRequest", orderRequest.toString()));
                }

            }
            if (!restaurantTableBookService.updateLastOrderPlacedAt(tableId)) {
                throw new RuntimeException("updateLastOrderPlacedAt failed.");
            }
            Boolean updatedOrderIds = restaurantTableBookService.updateOrderIdsInTabledBookedItem(tableId, orderIds);
            if (!updatedOrderIds) {
                throw new RuntimeException("OrderIds update failed for tableId");
            }
            logger.info("createOrder successfully processed!!", "createOrder", RestaurantOrderService.class.toString(), Map.of("orderRequest", orderRequest.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "Orders successfully placed.");
        } catch (Exception e) {
            logger.error("Exception raised in createOrder.", "createOrder", RestaurantOrderService.class.toString(), e, Map.of("orderRequest", orderRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error.", e);
        }

    }

    public RestaurantOrderControllerCheckOrderStatusResponse getOrderStatus(Long orderId) {
        try {
            logger.info("getOrderStatus called!!", "getOrderStatus", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                return new RestaurantOrderControllerCheckOrderStatusResponse().getNotFoundServerErrorResponse("Order item not found.");
            }
            OrderItem orderItem = orderItemOpt.get();
            RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData data = new RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData();
            data.setStatus(orderItem.getStatus().name());
            logger.info("getOrderStatus successfully processed!!", "getOrderStatus", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            return new RestaurantOrderControllerCheckOrderStatusResponse().getSuccessResponse(data, "getOrderStatus successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in getOrderStatus!!", "getOrderStatus", RestaurantOrderService.class.toString(), e, Map.of("orderId", orderId.toString()));
            return new RestaurantOrderControllerCheckOrderStatusResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }


    public RestaurantOrderControllerCheckOrderStatusResponse getOrderDetails(Long orderId) {
        try {
            logger.info("getOrderDetails called!!", "getOrderDetails", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                return new RestaurantOrderControllerCheckOrderStatusResponse().getNotFoundServerErrorResponse("Order item not found.");
            }
            OrderItem orderItem = orderItemOpt.get();
            FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(orderItem.getFoodMenuItemId());
            if (foodMenuItem == null) {
                logger.error("FoodMenuItem not found!!", "getOrderDetails", RestaurantOrderService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("foodMenuItem", orderItem.getFoodMenuItemId().toString(), "orderId", orderId.toString()));
                throw new RuntimeException("FoodMenuItem not found!!");
            }

            RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData data = new RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData();
            data.setStatus(orderItem.getStatus().name());
            data.setOrderPlacedAt(orderItem.getOrderPlacedAt().toString());
            data.setOrderServedAt(orderItem.getOrderServedAt().toString());
            data.setPrice(orderItem.getTotalPrice());
            data.setQuantity(orderItem.getQuantity());

            data.setFoodItemName(foodMenuItem.getName());
            logger.info("getOrderDetails successfully processed!!", "getOrderDetails", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            return new RestaurantOrderControllerCheckOrderStatusResponse().getSuccessResponse(data, "getOrderDetails successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in getOrderDetails!!", "getOrderDetails", RestaurantOrderService.class.toString(), e, Map.of("orderId", orderId.toString()));
            return new RestaurantOrderControllerCheckOrderStatusResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }


    public BaseDBOperationsResponse cancelOrder(Long orderId) {
        try {
            logger.info("deleteOrder called!!", "deleteOrder", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                logger.info("order item not found!!", "deleteOrder", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
                return new BaseDBOperationsResponse().getNotFoundServerErrorResponse("Order item not found.");
            }
            if (orderItemOpt.get().getStatus() != OrderItemStatusEnum.QUEUED) {
                logger.info("order item not in queued state!!", "deleteOrder", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Order can't be deleted as Order not in QUEUED state anymore.");
            }
            OrderItem orderItem = orderItemOpt.get();
            orderItem.setStatus(OrderItemStatusEnum.CANCELED);
            orderItemRepository.save(orderItem);
            logger.info("deleteOrder successfully processed!!", "deleteOrder", RestaurantOrderService.class.toString(), Map.of("orderId", orderId.toString()));
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "deleteOrder successfully processed.");
        } catch (Exception e) {
            logger.error("Exception raised in deleteOrder!!", "deleteOrder", RestaurantOrderService.class.toString(), e, Map.of("orderId", orderId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public RestaurantOrderControllerFetchAllOrdersByTableResponse fetchAllOrdersByTableId(Long tableId) {
        try {
            logger.info("fetchAllOrdersByTableId called!!", "fetchAllOrdersByTableId", RestaurantOrderService.class.toString(), Map.of("tableId", tableId.toString()));
            List<OrderItem> orderItems = orderItemRepository.findAllByTableItemId(tableId);
            RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData data = new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData();
            data.setOrders(new ArrayList<>());
            for (OrderItem orderItem : orderItems) {
                RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order order = new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order();
                order.setOrderId(orderItem.getId());
                order.setOrderStatus(orderItem.getStatus().name());
                data.getOrders().add(order);
            }
            logger.info("fetchAllOrdersByTableId successfully processed!!", "fetchAllOrdersByTableId", RestaurantOrderService.class.toString(), Map.of("tableId", tableId.toString()));
            return new RestaurantOrderControllerFetchAllOrdersByTableResponse().getSuccessResponse(data, "fetchAllOrdersByTableId successfully processed");
        } catch (Exception e) {
            logger.error("Exception raised in fetchAllOrdersByTableId!!", "fetchAllOrdersByTableId", RestaurantOrderService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new RestaurantOrderControllerFetchAllOrdersByTableResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }


}
