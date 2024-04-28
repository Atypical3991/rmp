package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.KitchenStockControllerRequestRefillRequest;
import com.biplab.dholey.rmp.models.api.request.KitchenStockControllerUpdateRefillRequestStatusRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.KitchenStockControllerCheckCurrentStockResponse;
import com.biplab.dholey.rmp.models.api.response.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse;
import com.biplab.dholey.rmp.models.db.StockItem;
import com.biplab.dholey.rmp.models.db.StockItemOrder;
import com.biplab.dholey.rmp.models.db.enums.StockItemOrderStatusEnum;
import com.biplab.dholey.rmp.repositories.StockItemOrderRepository;
import com.biplab.dholey.rmp.repositories.StockItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class KitchenStockService {

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private StockItemOrderRepository stockItemOrderRepository;

    public KitchenStockControllerCheckCurrentStockResponse checkCurrentStock(){
        KitchenStockControllerCheckCurrentStockResponse parentResponse = new KitchenStockControllerCheckCurrentStockResponse();
        try{
            List<StockItem> stockItems = stockItemRepository.findAllByQuantityGreaterThan(0L);
            if(stockItems.isEmpty()){
                parentResponse.setStatusCode(HttpStatus.NO_CONTENT.value());
                parentResponse.setError("No stockItems found!!");
                return parentResponse;
            }
            parentResponse.setData(new KitchenStockControllerCheckCurrentStockResponse.KitchenCookControllerPrepareFoodResponseResponseData());
            KitchenStockControllerCheckCurrentStockResponse.KitchenCookControllerPrepareFoodResponseResponseData response  = parentResponse.getData();
            List<KitchenStockControllerCheckCurrentStockResponse.KitchenCookControllerPrepareFoodResponseResponseData.Stock> stocks = new ArrayList<>();
            for(StockItem stockItem: stockItems){
                KitchenStockControllerCheckCurrentStockResponse.KitchenCookControllerPrepareFoodResponseResponseData.Stock stock =  new KitchenStockControllerCheckCurrentStockResponse.KitchenCookControllerPrepareFoodResponseResponseData.Stock();
                stock.setName(stockItem.getName());
                stock.setQuantity(stockItem.getQuantity());
                stock.setMetric(stockItem.getQuantityMetric());
                stocks.add(stock);
            }
            response.setStocks(stocks);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse createRefillKitchenStockRequest(KitchenStockControllerRequestRefillRequest refillStockRequest){
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try{
            StockItemOrder  stockItemOrder =  new StockItemOrder();
            stockItemOrder.setName(refillStockRequest.getName());
            stockItemOrder.setQuantity(refillStockRequest.getQuantity());
            stockItemOrder.setQuantityMetric(refillStockRequest.getMetric());
            stockItemOrder.setStatus(StockItemOrderStatusEnum.CREATED);
            stockItemOrderRepository.save(stockItemOrder);
            parentResponse.setData(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
            BaseDBOperationsResponse.BaseDBOperationsResponseResponseData response = parentResponse.getData();
            response.setSuccess(true);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse updateRefillRequestStatus(KitchenStockControllerUpdateRefillRequestStatusRequest updateRefillRequestStatus){
        BaseDBOperationsResponse parentResponse =  new BaseDBOperationsResponse();
        try{
            Optional<StockItemOrder> stockItemOrderOpt = stockItemOrderRepository.findById(updateRefillRequestStatus.getRefillRequestId());
            if (stockItemOrderOpt.isEmpty()){
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty stockItemOrderOpt returned for refillRequestId: "+updateRefillRequestStatus.getRefillRequestId());
                return parentResponse;
            }
            StockItemOrder stockItemOrder = stockItemOrderOpt.get();
            StockItemOrderStatusEnum stockItemOrderStatus =  StockItemOrderStatusEnum.valueOf(updateRefillRequestStatus.getStatus());
            if(!List.of(StockItemOrderStatusEnum.APPROVED,StockItemOrderStatusEnum.IN_PROGRESS,
                    StockItemOrderStatusEnum.PROCESSED,StockItemOrderStatusEnum.FAILED).contains(stockItemOrderStatus)){
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Not an acceptable status value!!");
                return  parentResponse;
            }
            stockItemOrder.setStatus(stockItemOrderStatus);
            stockItemOrderRepository.save(stockItemOrder);
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

    public KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse fetchAllRefillRequests(){
        KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse parentResponse = new KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse();
        try{
            List<StockItemOrder> stockItemOrders = stockItemOrderRepository.findAllByStatusIn(List.of(StockItemOrderStatusEnum.CREATED,StockItemOrderStatusEnum.IN_PROGRESS,StockItemOrderStatusEnum.PROCESSED,StockItemOrderStatusEnum.APPROVED));
            parentResponse.setData(new KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData());
            KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData response = new KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData();
            response.setRefillStockRequests(new ArrayList<>());
            for(StockItemOrder stockItemOrder: stockItemOrders){
                KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData.RefillStockRequest  refillRequest =  new KitchenStockControllerFetchAllInProgressRefillStockRequestsResponse.KitchenStockControllerFetchAllInProgressRefillStockRequestsResponseResponseData.RefillStockRequest();
                refillRequest.setStatus(stockItemOrder.getStatus().name());
                refillRequest.setRefillRequestId(stockItemOrder.getId());
                response.getRefillStockRequests().add(refillRequest);
            }
            parentResponse.setData(response);
            parentResponse.setStatusCode(HttpStatus.OK.value());
            return parentResponse;
        }catch (Exception e){
            parentResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
            parentResponse.setError(e.getMessage());
            return parentResponse;
        }
    }

    public BaseDBOperationsResponse deleteRefillStockOrderRequest(Long refillStockOrderRequestId){
        BaseDBOperationsResponse  parentResponse= new BaseDBOperationsResponse();
        try{
            Optional<StockItemOrder> stockItemOrderOpt = stockItemOrderRepository.findById(refillStockOrderRequestId);
            if(stockItemOrderOpt.isEmpty()){
                parentResponse.setStatusCode(HttpStatus.NOT_FOUND.value());
                parentResponse.setError("Empty stockItemOrderOpt returned for refillStockOrderRequestId: "+refillStockOrderRequestId);
                return parentResponse;
            }
            StockItemOrder stockItemOrder = stockItemOrderOpt.get();
            if(!List.of(StockItemOrderStatusEnum.CREATED,StockItemOrderStatusEnum.APPROVED).contains(stockItemOrder.getStatus())){
                parentResponse.setStatusCode(HttpStatus.NOT_ACCEPTABLE.value());
                parentResponse.setError("Not an acceptable status value!!");
                return parentResponse;
            }
            stockItemOrderRepository.deleteById(refillStockOrderRequestId);
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

}
