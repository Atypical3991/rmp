package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerAddItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.db.CartElementItem;
import com.biplab.dholey.rmp.models.db.CartItem;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.CartItemStatusEnum;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import com.biplab.dholey.rmp.repositories.CartElementItemRepository;
import com.biplab.dholey.rmp.repositories.CartItemRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.util.List;

import static com.biplab.dholey.rmp.common.CustomError.CommonErrors.INTERNAL_SERVER_ERROR;
import static com.biplab.dholey.rmp.common.CustomError.RestaurantCartServiceErrors.*;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantCartService.ADD_FOOD_ITEM_INTO_CART_SUCCESS_RESPONSE;
import static org.mockito.Mockito.when;

public class RestaurantCartServiceTest {

    @Mock
    private RestaurantTableService restaurantTableServiceMock;

    @Mock
    private RestaurantFoodMenuService restaurantFoodMenuServiceMock;

    @Mock
    private CartItemRepository cartItemRepositoryMock;

    @Mock
    private CartElementItemRepository cartElementItemRepository;

    @InjectMocks
    private RestaurantCartService restaurantCartService;


    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    //_____________________________________________ Unit Tests for addFoodItemIntoCart method __________________________
    @Test
    public void test_addFoodItemIntoCart_tableItemNotFoundCase() {
        RestaurantCartControllerAddItemRequest request = new RestaurantCartControllerAddItemRequest();
        request.setTableId(1L);

        when(restaurantTableServiceMock.fetchTableById(1L)).thenReturn(null);

        BaseDBOperationsResponse response = restaurantCartService.addFoodItemIntoCart(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        Assertions.assertEquals(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_FOUND_ERROR, response.getError());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR, response.getMessage());
        Assertions.assertNull(response.getData());
    }

    @Test
    public void test_addFoodItemIntoCart_tableItemNotInBookedStateCase() {
        RestaurantCartControllerAddItemRequest request = new RestaurantCartControllerAddItemRequest();
        request.setTableId(1L);

        TableItem tableItem = new TableItem();
        tableItem.setId(1L);
        tableItem.setStatus(TableItemStatusEnum.REMOVED);

        when(restaurantTableServiceMock.fetchTableById(1L)).thenReturn(tableItem);

        BaseDBOperationsResponse response = restaurantCartService.addFoodItemIntoCart(request);

        Assertions.assertEquals(HttpStatus.NOT_ACCEPTABLE.value(), response.getStatusCode());
        Assertions.assertEquals(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_IN_BOOKED_STATE_ERROR, response.getMessage());
        Assertions.assertNull(response.getError());
        Assertions.assertNull(response.getData());
    }

    @Test
    public void test_addFoodItemIntoCart_foodMenuItemNotFoundCase() {
        RestaurantCartControllerAddItemRequest request = new RestaurantCartControllerAddItemRequest();
        request.setTableId(1L);
        request.setFoodMenuItemId(1L);

        TableItem tableItem = new TableItem();
        tableItem.setId(1L);
        tableItem.setStatus(TableItemStatusEnum.BOOKED);

        when(restaurantTableServiceMock.fetchTableById(1L)).thenReturn(tableItem);

        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(null);


        BaseDBOperationsResponse response = restaurantCartService.addFoodItemIntoCart(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), response.getStatusCode());
        Assertions.assertEquals(ADD_FOOD_ITEM_INTO_CART_FOOD_MENU_ITEM_NOT_FOUND, response.getError());
        Assertions.assertEquals(INTERNAL_SERVER_ERROR, response.getMessage());
        Assertions.assertNull(response.getData());
    }

    @Test
    public void test_addFoodItemIntoCart_emptyCartSuccessCase() {
        RestaurantCartControllerAddItemRequest request = new RestaurantCartControllerAddItemRequest();
        request.setTableId(1L);
        request.setFoodMenuItemId(1L);
        request.setQuantity(10L);

        TableItem tableItem = new TableItem();
        tableItem.setId(1L);
        tableItem.setStatus(TableItemStatusEnum.BOOKED);

        FoodMenuItem foodMenuItem = new FoodMenuItem();
        foodMenuItem.setId(1L);
        foodMenuItem.setName("Creamy Pasta");
        foodMenuItem.setPrice(100D);

        CartItem cartItem = new CartItem();
        cartItem.setStatus(CartItemStatusEnum.ACTIVE);
        cartItem.setTableId(1L);
        cartItem.setTotalPrice(foodMenuItem.getPrice());
        cartItem.setCartElementIds(List.of(1L));

        CartElementItem cartElementItem = new CartElementItem();
        cartElementItem.setFoodItemId(foodMenuItem.getId());
        cartElementItem.setQuantity(request.getQuantity());

        when(restaurantTableServiceMock.fetchTableById(1L)).thenReturn(tableItem);
        when(restaurantFoodMenuServiceMock.getFoodMenuItemById(1L)).thenReturn(foodMenuItem);
        when(cartItemRepositoryMock.findByTableIdAndStatus(1L, CartItemStatusEnum.ACTIVE)).thenReturn(null);
        when(cartElementItemRepository.save(cartElementItem)).thenReturn(null);
        when(cartItemRepositoryMock.save(cartItem)).thenReturn(null);

        BaseDBOperationsResponse response = restaurantCartService.addFoodItemIntoCart(request);

        Assertions.assertEquals(HttpStatus.OK.value(), response.getStatusCode());
        Assertions.assertEquals(ADD_FOOD_ITEM_INTO_CART_SUCCESS_RESPONSE, response.getMessage());
        Assertions.assertNull(response.getError());
        Assertions.assertTrue(response.getData().getSuccess());

    }

    @Test
    public void test_addFoodItemIntoCart_nonEmptyCartCase() {
    }


    //_____________________________________________ Unit Tests for removeFoodItemFromCart method _______________________
    @Test
    public void test_removeFoodItemFromCart_tableItemNotFoundCase() {
    }

    @Test
    public void test_removeFoodItemFromCart_tableItemNotInBookedStateCase() {
    }

    @Test
    public void test_removeFoodItemFromCart_foodMenuItemNotFoundCase() {
    }

    @Test
    public void test_removeFoodItemFromCart_cartItemNullCase() {
    }

    @Test
    public void test_removeFoodItemFromCart_emptyCartCase() {
    }

    @Test
    public void test_removeFoodItemFromCart_successCase() {
    }

    //________________________________________ Unit Tests for fetchActiveCartItemsByTableId method _____________________
    @Test
    public void test_fetchActiveCartItemsByTableId_tableItemNotFoundCase() {
    }

    @Test
    public void test_fetchActiveCartItemsByTableId_tableNotInBookedStateCase() {
    }

    @Test
    public void test_fetchActiveCartItemsByTableId_emptyCartCase() {
    }

    @Test
    public void test_fetchActiveCartItemsByTableId_successCase() {
    }

    //______________________________________ Unit Tests for discardCartByTableId method ________________________________
    @Test
    public void test_discardCartByTableId_tableItemNotFoundCase() {
    }

    @Test
    public void test_discardCartByTableId_tableNotInBookedStateCase() {
    }

    @Test
    public void test_discardCartByTableId_cartItemNotFoundCase() {
    }

    @Test
    public void test_discardCartByTableId_successCase() {
    }
}
