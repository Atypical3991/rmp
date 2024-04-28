package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantOrderControllerCreateOrderRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerCheckOrderStatusResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantOrderControllerFetchAllOrdersByTableResponse;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import com.biplab.dholey.rmp.repositories.OrderItemRepository;
import com.fasterxml.jackson.databind.ser.Serializers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class RestaurantOrderService {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private FoodMenuService foodMenuService;

    private static final Long MAX_ORDER_QUEUED_COUNT = 50L;

    private static final Long MAX_ORDER_PER_REQUEST = 10L;


    public List<OrderItem> fetchAllOrdersByOrderIds(List<Long> orderIds){
        return orderItemRepository.findAllByIdIn(orderIds);
    }

    public List<OrderItem> fetchAllInProgressAndQueuedOrders(){
        return  orderItemRepository.findAllByStatusIn(List.of(OrderItemStatusEnum.PICKED_BY_COOK,OrderItemStatusEnum.QUEUED));
    }

    public Boolean updateOrderStatus(Long orderItemId, OrderItemStatusEnum statusEnum){
        try{
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderItemId);
            if(orderItemOpt.isEmpty()){
                return false;
            }
            orderItemOpt.get().setStatus(statusEnum);
            orderItemRepository.save(orderItemOpt.get());
            return true;
        }catch (Exception e){
            return false;
        }
    }


    @Transactional
    public BaseDBOperationsResponse createOrder(RestaurantOrderControllerCreateOrderRequest orderRequest){
        BaseDBOperationsResponse parentResponse = new BaseDBOperationsResponse();
        try{
            Long queuedOrdersCount = orderItemRepository.countByStatus(OrderItemStatusEnum.QUEUED);

            if (queuedOrdersCount >= MAX_ORDER_QUEUED_COUNT){
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Can't take anymore orders.");
                return parentResponse;
            }

            if(orderRequest.getOrderList().size() > MAX_ORDER_PER_REQUEST){
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Can't take more than "+ MAX_ORDER_PER_REQUEST+" orders.");
                return parentResponse;
            }
            for(RestaurantOrderControllerCreateOrderRequest.Order order :orderRequest.getOrderList()){
                FoodMenuItem foodMenuItem = foodMenuService.getFoodMenuItemById(order.getFoodMenuItemId());
                if(foodMenuItem == null){
                    parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                    parentResponse.setError("foodMenuItem not found for foodMenuItemId: "+order.getFoodMenuItemId());
                    return parentResponse;
                }
                OrderItem orderItem =  new OrderItem();
                orderItem.setFoodMenuItemId(order.getFoodMenuItemId());
                orderItem.setQuantity(order.getQuantity());
                orderItem.setTableItemId(order.getTableId());
                orderItem.setStatus(OrderItemStatusEnum.QUEUED);
                orderItemRepository.save(orderItem);
            }
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }

    }

    public RestaurantOrderControllerCheckOrderStatusResponse getOrderStatus(Long orderId){
        RestaurantOrderControllerCheckOrderStatusResponse parentResponse = new RestaurantOrderControllerCheckOrderStatusResponse();
        try{
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()){
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty orderItemOpt for orderId: "+orderId);
                return parentResponse;
            }
            OrderItem orderItem = orderItemOpt.get();
            parentResponse.setData(new RestaurantOrderControllerCheckOrderStatusResponse.RestaurantOrderControllerCheckOrderStatusResponseResponseData());
            parentResponse.getData().setStatus(orderItem.getStatus().name());
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse deleteOrder(Long orderId){
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try{
            Optional<OrderItem> orderItemOpt = orderItemRepository.findById(orderId);
            if (orderItemOpt.isEmpty()){
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty orderItemOpt returned for orderId: "+orderId);
                return parentResponse;
            }
            if (orderItemOpt.get().getStatus() != OrderItemStatusEnum.QUEUED){
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Order can't be deleted as Order not in QUEUED state anymore.");
                return parentResponse;
            }
            orderItemRepository.deleteById(orderId);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            parentResponse.getData().setSuccess(true);
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public RestaurantOrderControllerFetchAllOrdersByTableResponse fetchAllActiveOrdersByTableId(Long tableId){
        RestaurantOrderControllerFetchAllOrdersByTableResponse parentResponse =  new RestaurantOrderControllerFetchAllOrdersByTableResponse();
        try{
            List<OrderItem> orderItems = orderItemRepository.findAllByTableItemIdAndStatusNot(tableId,OrderItemStatusEnum.COMPLETED);
            parentResponse.setData(new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData());
            parentResponse.getData().setOrders(new ArrayList<>());
            for(OrderItem orderItem: orderItems){
                RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order order = new RestaurantOrderControllerFetchAllOrdersByTableResponse.RestaurantOrderControllerFetchAllOrdersByTableResponseResponseData.Order();
                order.setOrderId(orderItem.getId());
                order.setOrderStatus(orderItem.getStatus().name());
                parentResponse.getData().getOrders().add(order);
            }
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }
}
