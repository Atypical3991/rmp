package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.api.request.RestaurantBillControllerGenerateBillRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchAllBillsByTableIdResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantBillControllerFetchBillDetailsByBillIdResponse;
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
import java.util.Optional;

import static com.biplab.dholey.rmp.common.CustomError.CommonErrors.INTERNAL_SERVER_ERROR;
import static com.biplab.dholey.rmp.common.CustomError.RestaurantBillGenerationServiceErrors.*;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantBillService.FETCH_ALL_BILLS_BY_TABLE_ID;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantBillService.GENERATE_BILL_SUCCESS_RESPONSE;
import static com.biplab.dholey.rmp.common.constant.EnumToTextMap.BILL_STATUS_TO_TEXT_MAP;
import static org.mockito.Mockito.when;

//____________________________________________ Unit Tests of RestaurantBillService class _______________________________
public class RestaurantBillServiceTest {

    @Mock
    private RestaurantOrderService restaurantOrderServiceMock;

    @Mock
    private RestaurantTableBookService restaurantTableBookService;

    @Mock
    private RestaurantFoodMenuService restaurantFoodMenuServiceMock;

    @Mock
    private BillItemRepository billItemRepositoryMock;

    @InjectMocks
    private RestaurantBillService restaurantBillService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // _______________________ Unit Tests for generateBill method _____________________________________________________

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
        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(),response.getStatusCode());
        Assertions.assertNull(response.getData());
        Assertions.assertNull(response.getError());
        Assertions.assertEquals( GENERATE_BILL_NO_UN_BILLED_ORDER_FOUND_ERROR,response.getMessage());
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
        Assertions.assertEquals( HttpStatus.INTERNAL_SERVER_ERROR.value(),response.getStatusCode());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR,response.getMessage());
        Assertions.assertEquals(GENERATE_BILL_FOOD_MENU_ITEM_NOT_FOUND_ERROR,response.getError());

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
        Assertions.assertEquals( HttpStatus.INTERNAL_SERVER_ERROR.value(),response.getStatusCode());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR,response.getMessage());
        Assertions.assertEquals(GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR,response.getError());

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
        Assertions.assertEquals( HttpStatus.INTERNAL_SERVER_ERROR.value(),response.getStatusCode());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR,response.getMessage());
        Assertions.assertEquals(GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR,response.getError());
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

        when(billItemRepositoryMock.save(billItemRequest)).thenReturn(billItemResponse);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getError());
        Assertions.assertEquals( HttpStatus.OK.value(),response.getStatusCode());
        Assertions.assertEquals(GENERATE_BILL_SUCCESS_RESPONSE,response.getMessage());
        Assertions.assertEquals(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(),response.getData());
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

        when(billItemRepositoryMock.save(billItemRequest)).thenReturn(billItemResponse);

        BaseDBOperationsResponse response = restaurantBillService.generateBill(request);


        Assertions.assertNull(response.getError());
        Assertions.assertEquals( HttpStatus.OK.value(),response.getStatusCode());
        Assertions.assertEquals(GENERATE_BILL_SUCCESS_RESPONSE,response.getMessage());
        Assertions.assertEquals(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(),response.getData());
    }

    //______________________________________________ Unit Tests for fetchAllBillsByTableId method ______________________

    @Test
    public void test_fetchAllBillsByTableId_emptyListSuccess(){

        when(billItemRepositoryMock.findAllByTableItemId(1L)).thenReturn(List.of());
        RestaurantBillControllerFetchAllBillsByTableIdResponse response = restaurantBillService.fetchAllBillsByTableId(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals( List.of(),response.getData().getBills());
        Assertions.assertEquals(FETCH_ALL_BILLS_BY_TABLE_ID,response.getMessage());
    }

    @Test
    public void test_fetchAllBillsByTableId_nonEmptyListSuccess(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItem.setTableItemId(1L);
        billItem.setPayable(100D);
        billItem.setOrderItemIds(List.of(1L));

        when(billItemRepositoryMock.findAllByTableItemId(1L)).thenReturn(List.of(billItem));
        RestaurantBillControllerFetchAllBillsByTableIdResponse response = restaurantBillService.fetchAllBillsByTableId(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(1, response.getData().getBills().size());
        Assertions.assertEquals(billItem.getId(),response.getData().getBills().get(0).getBillId());
        Assertions.assertEquals(FETCH_ALL_BILLS_BY_TABLE_ID,response.getMessage());
    }

    //______________________________________ Unit Tests for fetchBillDetailsById method _______________________________

    @Test
    public void test_fetchBillDetailsById_emptyBillItem(){
        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.empty());

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1l);

        Assertions.assertEquals(HttpStatus.NOT_FOUND.value(),response.getStatusCode());
        Assertions.assertEquals( FETCH_BILL_DETAILS_BY_ID_BILL_ITEM_NOT_FOUND,response.getMessage());
        Assertions.assertNull(response.getData());
    }

    @Test
    public void test_fetchBillDetailsById_foodMenuItemNotFound(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.PAYMENT_SUCCESS);
        billItem.setOrderItemIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setId(1L);
        orderItem.setFoodMenuItemId(1L);
        orderItem.setTotalPrice(100D);

        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.of(billItem));
        when(restaurantOrderServiceMock.fetchAllOrdersByOrderIds(List.of(1L))).thenReturn(List.of(orderItem));
        when((restaurantFoodMenuServiceMock.getFoodMenuItemById(1L))).thenReturn(null);

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1l);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(),response.getStatusCode());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR,response.getMessage());
        Assertions.assertEquals(FETCH_BILL_DETAILS_BY_ID_FOOD_MENU_ITEM_NOT_FOUND,response.getError());
        Assertions.assertNull(response.getData());
    }

    @Test
    public void test_fetchBillDetailsById__success_paymentSuccessBillStatusWithMultiOrders(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.PAYMENT_SUCCESS);
        billItem.setOrderItemIds(List.of(1L,2L));

        OrderItem orderItemOne =  new OrderItem();
        orderItemOne.setId(1L);
        orderItemOne.setFoodMenuItemId(1L);
        orderItemOne.setTotalPrice(100D);

        OrderItem orderItemTwo =  new OrderItem();
        orderItemTwo.setId(2L);
        orderItemTwo.setFoodMenuItemId(1L);
        orderItemTwo.setTotalPrice(100D);

        FoodMenuItem foodMenuItem =  new FoodMenuItem();
        foodMenuItem.setName("Creamy Pasta");


        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.of(billItem));
        when(restaurantOrderServiceMock.fetchAllOrdersByOrderIds(List.of(1L,2L))).thenReturn(List.of(orderItemOne,orderItemTwo));
        when((restaurantFoodMenuServiceMock.getFoodMenuItemById(1L))).thenReturn(foodMenuItem);

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());;
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(2,response.getData().getOrdersList().size());
        Assertions.assertEquals(orderItemOne.getId(),response.getData().getOrdersList().get(0).getOrderId());
        Assertions.assertEquals(orderItemTwo.getId(),response.getData().getOrdersList().get(1).getOrderId());
        Assertions.assertEquals(orderItemOne.getTotalPrice(),response.getData().getOrdersList().get(0).getTotalPrice());
        Assertions.assertEquals(orderItemTwo.getTotalPrice(),response.getData().getOrdersList().get(1).getTotalPrice());
        Assertions.assertEquals(foodMenuItem.getName(),response.getData().getOrdersList().get(0).getFoodItemName());
        Assertions.assertEquals(foodMenuItem.getName(),response.getData().getOrdersList().get(1).getFoodItemName());
        Assertions.assertEquals(orderItemOne.getTotalPrice()+orderItemTwo.getTotalPrice(),response.getData().getTotalPayable());
        Assertions.assertEquals(BILL_STATUS_TO_TEXT_MAP.get(BillItemStatusEnum.PAYMENT_SUCCESS), response.getData().getPaymentStatus());
    }
    @Test
    public void test_fetchBillDetailsById_success_paymentInitiatedBillStatus(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.PAYMENT_INITIATED);
        billItem.setOrderItemIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setId(1L);
        orderItem.setFoodMenuItemId(1L);
        orderItem.setTotalPrice(100D);

        FoodMenuItem foodMenuItem =  new FoodMenuItem();
        foodMenuItem.setName("Creamy Pasta");


        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.of(billItem));
        when(restaurantOrderServiceMock.fetchAllOrdersByOrderIds(List.of(1L))).thenReturn(List.of(orderItem));
        when((restaurantFoodMenuServiceMock.getFoodMenuItemById(1L))).thenReturn(foodMenuItem);

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());;
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(1,response.getData().getOrdersList().size());
        Assertions.assertEquals(orderItem.getId(),response.getData().getOrdersList().get(0).getOrderId());
        Assertions.assertEquals(orderItem.getTotalPrice(),response.getData().getOrdersList().get(0).getTotalPrice());
        Assertions.assertEquals(foodMenuItem.getName(),response.getData().getOrdersList().get(0).getFoodItemName());
        Assertions.assertEquals(orderItem.getTotalPrice(),response.getData().getTotalPayable());
        Assertions.assertEquals(BILL_STATUS_TO_TEXT_MAP.get(BillItemStatusEnum.PAYMENT_INITIATED), response.getData().getPaymentStatus());
    }

    @Test
    public void test_fetchBillDetailsById_success_paymentFailedBillStatus(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.PAYMENT_FAILED);
        billItem.setOrderItemIds(List.of(1L));

        OrderItem orderItem =  new OrderItem();
        orderItem.setId(1L);
        orderItem.setFoodMenuItemId(1L);
        orderItem.setTotalPrice(100D);

        FoodMenuItem foodMenuItem =  new FoodMenuItem();
        foodMenuItem.setName("Creamy Pasta");


        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.of(billItem));
        when(restaurantOrderServiceMock.fetchAllOrdersByOrderIds(List.of(1L))).thenReturn(List.of(orderItem));
        when((restaurantFoodMenuServiceMock.getFoodMenuItemById(1L))).thenReturn(foodMenuItem);

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());;
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(1,response.getData().getOrdersList().size());
        Assertions.assertEquals(orderItem.getId(),response.getData().getOrdersList().get(0).getOrderId());
        Assertions.assertEquals(orderItem.getTotalPrice(),response.getData().getOrdersList().get(0).getTotalPrice());
        Assertions.assertEquals(foodMenuItem.getName(),response.getData().getOrdersList().get(0).getFoodItemName());
        Assertions.assertEquals(orderItem.getTotalPrice(),response.getData().getTotalPayable());
        Assertions.assertEquals(BILL_STATUS_TO_TEXT_MAP.get(BillItemStatusEnum.PAYMENT_FAILED), response.getData().getPaymentStatus());
    }

    @Test
    public void test_fetchBillDetailsById_emptyOrderItemsList(){
        BillItem billItem =  new BillItem();
        billItem.setId(1L);
        billItem.setStatus(BillItemStatusEnum.BILL_GENERATED);
        billItem.setOrderItemIds(List.of(1L));


        FoodMenuItem foodMenuItem =  new FoodMenuItem();
        foodMenuItem.setName("Creamy Pasta");


        when(billItemRepositoryMock.findById(1L)).thenReturn(Optional.of(billItem));
        when(restaurantOrderServiceMock.fetchAllOrdersByOrderIds(List.of(1L))).thenReturn(List.of());
        when((restaurantFoodMenuServiceMock.getFoodMenuItemById(1L))).thenReturn(foodMenuItem);

        RestaurantBillControllerFetchBillDetailsByBillIdResponse response = restaurantBillService.fetchBillDetailsById(1L);

        Assertions.assertEquals(HttpStatus.OK.value(),response.getStatusCode());;
        Assertions.assertNotNull(response.getData());
        Assertions.assertEquals(0,response.getData().getOrdersList().size());
        Assertions.assertEquals(0,response.getData().getTotalPayable());
        Assertions.assertEquals(BILL_STATUS_TO_TEXT_MAP.get(BillItemStatusEnum.BILL_GENERATED),response.getData().getPaymentStatus());
    }

}
