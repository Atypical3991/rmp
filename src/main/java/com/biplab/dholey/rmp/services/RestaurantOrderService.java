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
import com.biplab.dholey.rmp.models.util.PrepareFoodTaskQueueModel;
import com.biplab.dholey.rmp.repositories.OrderItemRepository;
import com.biplab.dholey.rmp.util.TaskQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RestaurantOrderService {

    private static final Long MAX_ORDER_QUEUED_COUNT = 50L;
    private static final Long MAX_ORDER_PER_REQUEST = 10L;
    private final TaskQueue prepareFoodTaskQueue = new TaskQueue("prepare_food_task_queue");
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private FoodMenuService foodMenuService;
    @Autowired
    private RestaurantTableService restaurantTableService;

    @Autowired
    private RestaurantTableBookService restaurantTableBookService;

    public List<OrderItem> fetchAllOrdersByOrderIds(List<Long> orderIds) {
        return orderItemRepository.findAllByIdIn(orderIds);
    }

    public List<OrderItem> fetchAllUnBilledOrders(List<Long> orderItemIds) {
        return orderItemRepository.findAllByIdInAndBillGenerated(orderItemIds, false);
    }

    public List<OrderItem> fetchAllInProgressAndQueuedOrders() {
        return orderItemRepository.findAllByStatusIn(List.of(OrderItemStatusEnum.PICKED_BY_COOK, OrderItemStatusEnum.QUEUED));
    }

    public Boolean updateOrderStatus(Long orderItemId, OrderItemStatusEnum statusEnum) {
        try {
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
            if (orderItemOpt.isEmpty()) {
                return false;
            }
            orderItemOpt.get().setStatus(statusEnum);
            orderItemRepository.save(orderItemOpt.get());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public OrderItem fetchOrderById(Long orderItemId) {
        try {
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
            return orderItemOpt.orElse(null);
        } catch (Exception e) {
            return null;
        }
    }


    public Map<Long, List<OrderItem>> fetchReadyToServeOrders() {
        try {
            List<OrderItem> orderItemList = orderItemRepository.findAllByStatusIn(List.of(OrderItemStatusEnum.READY_TO_SERVE));
            return orderItemList.stream().collect(Collectors.groupingBy(OrderItem::getTableItemId));
        } catch (Exception e) {
            return null;
        }
    }

    public Boolean updateOrderStatusByListOfOrderIds(OrderItemStatusEnum statusEnum, List<Long> orderIds) {
        try {
            orderItemRepository.updateOrderStatusByOrderIdsDBQuery(statusEnum, orderIds);
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    public PrepareFoodTaskQueueModel popQueuedPrepareFoodTasks() {
        return (PrepareFoodTaskQueueModel) prepareFoodTaskQueue.popTask();
    }

    public boolean updateBillGenerationStatus(Long orderId) {
        try {
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                return false;
            }
            orderItemOpt.get().setBillGenerated(true);
            orderItemRepository.save(orderItemOpt.get());
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    @Transactional
    public BaseDBOperationsResponse createOrder(RestaurantOrderControllerCreateOrderRequest orderRequest) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            Long queuedOrdersCount = orderItemRepository.countByStatus(OrderItemStatusEnum.QUEUED);

            if (queuedOrdersCount >= MAX_ORDER_QUEUED_COUNT) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Can't take anymore orders.");
                return parentResponse;
            }

            if (orderRequest.getOrderList().size() > MAX_ORDER_PER_REQUEST) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Can't take more than " + MAX_ORDER_PER_REQUEST + " orders.");
                return parentResponse;
            }
            Long tableId = orderRequest.getTableId();
            TableBookedItem tableBookedItem = restaurantTableBookService.getTableBookedItemByTableId(tableId);
            if (tableBookedItem == null) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("TableBookItem not  found for tableId: " + tableId);
                return parentResponse;
            }
            if (!Objects.equals(BookedTableStatusEnum.BOOKED, tableBookedItem.getStatus())) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Can't table anymore orders for tableId: " + tableId);
                return parentResponse;
            }

            List<Long> orderIds = new ArrayList<>();
            for (RestaurantOrderControllerCreateOrderRequest.Order order : orderRequest.getOrderList()) {

                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(order.getFoodMenuItemId());
                if (foodMenuItem == null) {
                    parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                    parentResponse.setError("foodMenuItem not found for foodMenuItemId: " + order.getFoodMenuItemId());
                    return parentResponse;
                }
                OrderItem orderItem = new OrderItem();
                orderItem.setFoodMenuItemId(order.getFoodMenuItemId());
                orderItem.setQuantity(order.getQuantity());
                orderItem.setTableItemId(tableId);
                orderItem.setStatus(OrderItemStatusEnum.QUEUED);
                orderItemRepository.save(orderItem);
                orderIds.add(orderItem.getId());


                PrepareFoodTaskQueueModel prepareFoodTaskQueueModel = new PrepareFoodTaskQueueModel();
                prepareFoodTaskQueueModel.setOrderId(orderItem.getId());
                prepareFoodTaskQueueModel.setTableId(orderItem.getTableItemId());
                prepareFoodTaskQueue.pushTask(prepareFoodTaskQueueModel);
            }
            restaurantTableBookService.updateLastOrderPlacedAt(tableId);
            Boolean updatedOrderIds = restaurantTableBookService.updateOrderIdsInTabledBookedItem(tableId, orderIds);
            if (!updatedOrderIds) {
                throw new RuntimeException("OrderIds update failed for tableId");
            }


            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }

    }

    public RestaurantOrderControllerCheckOrderStatusResponse getOrderStatus(Long orderId) {
        RestaurantOrderControllerCheckOrderStatusResponse parentResponse = new RestaurantOrderControllerCheckOrderStatusResponse();
        try {
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty orderItemOpt for orderId: " + orderId);
                return parentResponse;
            }
            OrderItem orderItem = orderItemOpt.get();
            parentResponse.setData(new RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData());
            parentResponse.getData().setStatus(orderItem.getStatus().name());
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse deleteOrder(Long orderId) {
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try {
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()) {
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty orderItemOpt returned for orderId: " + orderId);
                return parentResponse;
            }
            if (orderItemOpt.get().getStatus() != OrderItemStatusEnum.QUEUED) {
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Order can't be deleted as Order not in QUEUED state anymore.");
                return parentResponse;
            }
            OrderItem orderItem = orderItemOpt.get();
            orderItem.setStatus(OrderItemStatusEnum.DELETED);
            orderItemRepository.save(orderItem);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public RestaurantOrderControllerFetchAllOrdersByTableResponse fetchAllOrdersByTableId(Long tableId) {
        RestaurantOrderControllerFetchAllOrdersByTableResponse parentResponse = new RestaurantOrderControllerFetchAllOrdersByTableResponse();
        try {
            List<OrderItem> orderItems = orderItemRepository.findAllByTableItemId(tableId);
            parentResponse.setData(new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData());
            parentResponse.getData().setOrders(new ArrayList<>());
            for (OrderItem orderItem : orderItems) {
                RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order order = new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order();
                order.setOrderId(orderItem.getId());
                order.setOrderStatus(orderItem.getStatus().name());
                parentResponse.getData().getOrders().add(order);
            }
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        } catch (Exception e) {
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }


}
