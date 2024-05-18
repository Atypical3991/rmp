package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.db.BillItem;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.biplab.dholey.rmp.repositories.BillItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static com.biplab.dholey.rmp.common.CustomError.CommonErrors.INTERNAL_SERVER_ERROR;
import static com.biplab.dholey.rmp.common.CustomError.RestaurantBillGenerationServiceErrors.*;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantBillService.GENERATE_BILL_SUCCESS_RESPONSE;
import static org.mockito.Mockito.when;


public class RestaurantBillServiceTest {

    @Mock
    private RestaurantOrderService restaurantOrderServiceMock;

    @Mock
    private RestaurantTableBookService restaurantTableBookService;

    @Mock
    private RestaurantFoodMenuService restaurantFoodMenuServiceMock;

    @Mock
    private BillItemRepository billItemRepository;

    @InjectMocks
    private RestaurantBillService restaurantBillService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    public void test_generateBill_with_emptyOrdersItemList(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.NOT_ACCEPTABLE.value());
        Assertions.assertNull(response.getData());
        Assertions.assertNull(response.getError());
        Assertions.assertEquals(response.getMessage(),  GENERATE_BILL_EMPTY_ORDERS_LIST_IN_REQUEST_ERROR);
    }

    @Test
    public void test_generateBill_with_emptyUnBilledOrdersItemList(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(1L));

        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(1L))).thenReturn(new ArrayList<>());

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.NOT_FOUND.value());
        Assertions.assertNull(response.getData());
        Assertions.assertNull(response.getError());
        Assertions.assertEquals(response.getMessage(),  GENERATE_BILL_NO_UN_BILLED_ORDER_FOUND_ERROR);
    }

    @Test
    public void test_generateBill_with_nullFoodMenuItemForGivenOrder(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setFoodMenuItemId(1L);

        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(1L))).thenReturn(List.of(orderItem));
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(null);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);

        Assertions.assertNull(response.getData());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertEquals(response.getMessage(),INTERNAL_SERVER_ERROR);
        Assertions.assertEquals(response.getError(),GENERATE_BILL_FOOD_MENU_ITEM_NOT_FOUND_ERROR);

    }

    @Test
    public void test_generateBill_with_updateBillGenerationStatusFailure(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setFoodMenuItemId(1L);

        FoodMenuItem foodMenuItem = new FoodMenuItem();
        foodMenuItem.setPrice(100D);


        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(1L))).thenReturn(List.of(orderItem));
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(foodMenuItem);
        when(restaurantOrderServiceMock.updateBillGenerationStatus(1L)).thenReturn(false);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getData());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertEquals(response.getMessage(),INTERNAL_SERVER_ERROR);
        Assertions.assertEquals(response.getError(),GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR);

    }

    @Test
    public void test_generateBill_with_updateBillGenerateAtFailure(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setFoodMenuItemId(1L);

        FoodMenuItem foodMenuItem = new FoodMenuItem();
        foodMenuItem.setPrice(100D);


        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(1L))).thenReturn(List.of(orderItem));
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(foodMenuItem);
        when(restaurantOrderServiceMock.updateBillGenerationStatus(1L)).thenReturn(true);
        when(restaurantTableBookService.updateBillGenerateAt(1L)).thenReturn(false);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getData());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
        Assertions.assertEquals(response.getMessage(),INTERNAL_SERVER_ERROR);
        Assertions.assertEquals(response.getError(),GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR);
    }

    @Test
    public void test_generateBill_with_singleOrderItemSuccess(){
        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setFoodMenuItemId(1L);
        orderItem.setId(request.getOrderIds().get(0));
        orderItem.setQuantity(100L);

        FoodMenuItem foodMenuItem = new FoodMenuItem();
        foodMenuItem.setPrice(100D);


        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(1L))).thenReturn(List.of(orderItem));
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(foodMenuItem);
        when(restaurantOrderServiceMock.updateBillGenerationStatus(1L)).thenReturn(true);
        when(restaurantTableBookService.updateBillGenerateAt(1L)).thenReturn(true);

        BillItem billItemRequest = new BillItem();
        billItemRequest.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItemRequest.setTableItemId(request.getTableItemId());
        billItemRequest.setPayable(orderItem.getTotalPrice()*orderItem.getQuantity());
        billItemRequest.setOrderItemIds(List.of(orderItem.getId()));

        BillItem billItemResponse = new BillItem();
        billItemResponse.setId(100L);
        billItemRequest.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItemRequest.setTableItemId(request.getTableItemId());
        billItemRequest.setPayable(orderItem.getTotalPrice()*orderItem.getQuantity());
        billItemRequest.setOrderItemIds(List.of(orderItem.getId()));

        when(billItemRepository.save(billItemRequest)).thenReturn(billItemResponse);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getError());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK.value());
        Assertions.assertEquals(response.getMessage(),GENERATE_BILL_SUCCESS_RESPONSE);
        Assertions.assertEquals(response.getData(),new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
    }

    @Test
    public void test_generateBill_with_multiOrderItemsSuccess(){
        OrderItem orderItemOne =  new OrderItem();
        orderItemOne.setFoodMenuItemId(1L);
        orderItemOne.setId(1L);
        orderItemOne.setQuantity(100L);

        OrderItem orderItemTwo =  new OrderItem();
        orderItemTwo.setFoodMenuItemId(1L);
        orderItemTwo.setId(2L);
        orderItemTwo.setQuantity(100L);

        RestaurantBillControllerGenerateBillRequest request =  new RestaurantBillControllerGenerateBillRequest();
        request.setTableItemId(1L);
        request.setOrderIds(List.of(orderItemOne.getId(), orderItemTwo.getId()));


        FoodMenuItem foodMenuItem = new FoodMenuItem();
        foodMenuItem.setId(1L);
        foodMenuItem.setPrice(100D);


        when(restaurantOrderServiceMock.fetchAllUnBilledOrders(List.of(orderItemOne.getId(), orderItemTwo.getId()))).thenReturn(List.of(orderItemOne,orderItemTwo));
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(foodMenuItem.getId())).thenReturn(foodMenuItem);
        when(restaurantOrderServiceMock.updateBillGenerationStatus(orderItemOne.getId())).thenReturn(true);
        when(restaurantOrderServiceMock.updateBillGenerationStatus(orderItemTwo.getId())).thenReturn(true);
        when(restaurantTableBookService.updateBillGenerateAt(orderItemOne.getId())).thenReturn(true);
        when(restaurantTableBookService.updateBillGenerateAt(orderItemTwo.getId())).thenReturn(true);


        BillItem billItemRequest = new BillItem();
        billItemRequest.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItemRequest.setTableItemId(request.getTableItemId());
        billItemRequest.setPayable((orderItemOne.getTotalPrice()*orderItemOne.getQuantity())+(orderItemTwo.getTotalPrice()*orderItemTwo.getQuantity()));
        billItemRequest.setOrderItemIds(List.of(orderItemOne.getId(), orderItemTwo.getId()));

        BillItem billItemResponse = new BillItem();
        billItemResponse.setId(100L);
        billItemRequest.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItemRequest.setTableItemId(request.getTableItemId());
        billItemRequest.setPayable((orderItemOne.getTotalPrice()*orderItemOne.getQuantity())+(orderItemTwo.getTotalPrice()*orderItemTwo.getQuantity()));
        billItemRequest.setOrderItemIds(List.of(orderItemOne.getId(), orderItemTwo.getId()));

        when(billItemRepository.save(billItemRequest)).thenReturn(billItemResponse);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getError());
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK.value());
        Assertions.assertEquals(response.getMessage(),GENERATE_BILL_SUCCESS_RESPONSE);
        Assertions.assertEquals(response.getData(),new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData());
    }
}
