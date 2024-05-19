package com.biplab.dholey.rmp.services;


import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerAddItemRequest;
import com.biplab.dholey.rmp.models.api.request.RestaurantCartControllerRemoveItemRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantCartControllerFetchActiveCartItemsByTableIdResponse;
import com.biplab.dholey.rmp.models.db.CartElementItem;
import com.biplab.dholey.rmp.models.db.CartItem;
import com.biplab.dholey.rmp.models.db.FoodMenuItem;
import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.CartItemStatusEnum;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import com.biplab.dholey.rmp.repositories.CartElementItemRepository;
import com.biplab.dholey.rmp.repositories.CartItemRepository;
import com.biplab.dholey.rmp.util.CustomLogger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.biplab.dholey.rmp.common.CustomError.CommonErrors.INTERNAL_SERVER_ERROR;
import static com.biplab.dholey.rmp.common.CustomError.RestaurantCartServiceErrors.*;
import static com.biplab.dholey.rmp.common.CustomSuccessMessage.RestaurantCartService.ADD_FOOD_ITEM_INTO_CART_SUCCESS_RESPONSE;

@Service
public class RestaurantCartService {

    private final CustomLogger logger = new CustomLogger(LoggerFactory.getLogger(RestaurantCartService.class));
    @Autowired
    private RestaurantTableService restaurantTableService;
    @Autowired
    private CartElementItemRepository cartElementItemRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private RestaurantFoodMenuService restaurantFoodMenuService;

    public boolean updateOrderPlacedInCartItem(Long cartId) {
        try {
            logger.info("fetchCartItemById called!!", "fetchCartItemById", RestaurantCartService.class.toString(), Map.of("cartId", cartId.toString()));
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartId);
            if (cartItemOpt.isEmpty()) {
                throw new RuntimeException("Cart item not found!!");
            }
            cartItemOpt.get().setStatus(CartItemStatusEnum.ORDER_PLACED);
            cartItemRepository.save(cartItemOpt.get());
            return true;
        } catch (Exception e) {
            logger.error("updateOrderPlacedInCartItem call failed", "updateOrderPlacedInCartItem", RestaurantCartService.class.toString(), e, Map.of("cartId", cartId.toString()));
            return false;
        }
    }

    public CartItem fetchActiveCartItemByTableId(Long tableId) {
        try {
            logger.info("fetchCartItemById called!!", "fetchCartItemById", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
            return cartItemRepository.findByTableIdAndStatus(tableId, CartItemStatusEnum.ACTIVE);
        } catch (Exception e) {
            logger.error("fetchCartItemById call failed", "fetchCartItemById", RestaurantCartService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return null;
        }
    }

    public List<CartElementItem> fetchCartElementItemsByCartId(Long cartId) {
        try {
            logger.info("fetchCartItemById called!!", "fetchCartItemById", RestaurantCartService.class.toString(), Map.of("cartId", cartId.toString()));
            Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartId);
            if (cartItemOpt.isEmpty()) {
                throw new RuntimeException("Cart item not found!!");
            }
            CartItem cartItem = cartItemOpt.get();
            return cartElementItemRepository.findAllByIdIn(cartItem.getCartElementIds());
        } catch (Exception e) {
            logger.error("fetchCartElementItemsByCartId call failed", "fetchCartElementItemsByCartId", RestaurantCartService.class.toString(), e, Map.of("cartId", cartId.toString()));
            return null;
        }
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse addFoodItemIntoCart(RestaurantCartControllerAddItemRequest restaurantCartControllerAddItemRequest) {
        try {
            logger.info("addFoodItemIntoCart called!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
            Long tableId = restaurantCartControllerAddItemRequest.getTableId();
            TableItem tableItem = restaurantTableService.fetchTableById(tableId);
            if (tableItem == null) {
                logger.error("addFoodItemIntoCart called!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), new RuntimeException(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_FOUND_ERROR), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                throw new RuntimeException(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_FOUND_ERROR);
            }
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                logger.error("Table not in booked state!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), new RuntimeException(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_IN_BOOKED_STATE_ERROR), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse(ADD_FOOD_ITEM_INTO_CART_TABLE_NOT_IN_BOOKED_STATE_ERROR);
            }
            FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(restaurantCartControllerAddItemRequest.getFoodMenuItemId());
            if (foodMenuItem == null) {
                logger.error("addFoodItemIntoCart called!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), new RuntimeException(ADD_FOOD_ITEM_INTO_CART_FOOD_MENU_ITEM_NOT_FOUND), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                throw new RuntimeException(ADD_FOOD_ITEM_INTO_CART_FOOD_MENU_ITEM_NOT_FOUND);
            }

            CartItem cartItem = cartItemRepository.findByTableIdAndStatus(tableId, CartItemStatusEnum.ACTIVE);
            if (cartItem == null) {
                cartItem = new CartItem();
                cartItem.setTableId(tableId);
                cartItem.setCartElementIds(new ArrayList<>());
                cartItem.setTotalPrice(0D);
                cartItem.setStatus(CartItemStatusEnum.ACTIVE);
            }

            if (!cartItem.getCartElementIds().isEmpty()) {
                List<CartElementItem> cartElementItemList = cartElementItemRepository.findAllByIdIn(cartItem.getCartElementIds());
                for (CartElementItem cartElementItem : cartElementItemList) {
                    if (Objects.equals(cartElementItem.getFoodItemId(), foodMenuItem.getId())) {
                        cartElementItem.setQuantity(cartElementItem.getQuantity() + restaurantCartControllerAddItemRequest.getQuantity());
                        cartElementItemRepository.save(cartElementItem);
                        cartItem.setTotalPrice(cartItem.getTotalPrice() + (foodMenuItem.getPrice() * restaurantCartControllerAddItemRequest.getQuantity()));
                        cartItemRepository.save(cartItem);
                        logger.info("cartElementItem found!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                        return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "addFoodItemIntoCart successfully processed!!");
                    }
                }

                CartElementItem cartElementItem = new CartElementItem();
                cartElementItem.setQuantity(restaurantCartControllerAddItemRequest.getQuantity());
                cartElementItem.setFoodItemId(foodMenuItem.getId());
                cartElementItemRepository.save(cartElementItem);

                cartItem.getCartElementIds().add(cartElementItem.getId());
                cartItemRepository.save(cartItem);
                logger.info("addFoodItemIntoCart successfully processed!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), ADD_FOOD_ITEM_INTO_CART_SUCCESS_RESPONSE);

            } else {
                CartElementItem cartElementItem = new CartElementItem();
                cartElementItem.setFoodItemId(foodMenuItem.getId());
                cartElementItem.setQuantity(restaurantCartControllerAddItemRequest.getQuantity());
                cartElementItemRepository.save(cartElementItem);

                cartItem.getCartElementIds().add(cartElementItem.getId());
                cartItem.setTotalPrice(foodMenuItem.getPrice() * cartElementItem.getQuantity());
                cartItemRepository.save(cartItem);
                logger.info("addFoodItemIntoCart successfully processed!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
                return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), ADD_FOOD_ITEM_INTO_CART_SUCCESS_RESPONSE);
            }

        } catch (Exception e) {
            logger.error("addFoodItemIntoCart called!!", "addFoodItemIntoCart", RestaurantCartService.class.toString(), e, Map.of("restaurantCartControllerAddItemRequest", restaurantCartControllerAddItemRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse(INTERNAL_SERVER_ERROR, e);

        }
    }


    @Transactional(isolation = Isolation.SERIALIZABLE)
    public BaseDBOperationsResponse removeFoodItemFromCart(RestaurantCartControllerRemoveItemRequest restaurantCartControllerRemoveItemRequest) {
        try {
            logger.info("removeFoodItemFromCart called!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
            Long tableId = restaurantCartControllerRemoveItemRequest.getTableId();
            TableItem tableItem = restaurantTableService.fetchTableById(tableId);
            if (tableItem == null) {
                logger.error("removeFoodItemFromCart called!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), new RuntimeException("Table not found!!"), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
                throw new RuntimeException("Table not found!!");
            }
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                logger.info("Table not in booked state!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Table not in booked state.So, add items can't be Removed from Cart.");
            }
            FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(restaurantCartControllerRemoveItemRequest.getFoodMenuItemId());
            if (foodMenuItem == null) {
                logger.error("addFoodItemIntoCart called!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
                throw new RuntimeException("FoodMenuItem not found!!");
            }
            CartItem cartItem = cartItemRepository.findByTableIdAndStatus(tableId, CartItemStatusEnum.ACTIVE);
            if (cartItem == null) {
                logger.info("CartItem not found!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("CartItem not found!!");
            }
            List<Long> cartElementItemsIdsList = cartItem.getCartElementIds();
            List<CartElementItem> cartElementItemList = cartElementItemRepository.findAllByIdIn(cartElementItemsIdsList);
            if (cartElementItemList.isEmpty()) {
                logger.info("No CartElementItem not found!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("No CartElementItem not found!!");
            }
            for (CartElementItem cartElementItem : cartElementItemList) {
                if (cartElementItem.getFoodItemId().equals(foodMenuItem.getId())) {
                    if (cartElementItem.getQuantity() == 1) {
                        cartItem.setTotalPrice(cartItem.getTotalPrice() - foodMenuItem.getPrice());
                        cartItem.getCartElementIds().remove(cartElementItem.getId());
                        cartItemRepository.save(cartItem);
                        cartElementItemRepository.delete(cartElementItem);
                    } else {
                        cartItem.setTotalPrice(cartItem.getTotalPrice() - foodMenuItem.getPrice());
                        cartItemRepository.save(cartItem);
                        cartElementItem.setQuantity(cartElementItem.getQuantity() - 1);
                        cartElementItemRepository.save(cartElementItem);

                    }
                    return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "removeFoodItemFromCart successfully processed!!");
                }
            }
            return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("No CartElementItem not found for given foodMenuItem!!");
        } catch (Exception e) {
            logger.error("removeFoodItemFromCart called!!", "removeFoodItemFromCart", RestaurantCartService.class.toString(), e, Map.of("restaurantCartControllerRemoveItemRequest", restaurantCartControllerRemoveItemRequest.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }


    public RestaurantCartControllerFetchActiveCartItemsByTableIdResponse fetchActiveCartItemsByTableId(Long tableId) {
        try {
            logger.info("fetchActiveCartItemsByTableId called!!", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
            TableItem tableItem = restaurantTableService.fetchTableById(tableId);
            if (tableItem == null) {
                logger.error("fetchActiveCartItemsByTableId called!!", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), new RuntimeException("Table not found!!"), Map.of("tableId", tableId.toString()));
                throw new RuntimeException("Table not found!!");
            }
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                logger.info("Table not in booked state!!", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
                return new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse().getNotAcceptableServerErrorResponse("Table not in booked state.So, add items can't be Removed from Cart.");
            }

            CartItem cartItem = cartItemRepository.findByTableIdAndStatus(tableId, CartItemStatusEnum.ACTIVE);
            if (cartItem == null) {
                logger.info("No active CartItem found", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
                return new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse().getNotAcceptableServerErrorResponse("No active CartItem found");
            }
            RestaurantCartControllerFetchActiveCartItemsByTableIdResponse.RestaurantCartControllerFetchActiveCartItemsByTableIdResponseData data = new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse.RestaurantCartControllerFetchActiveCartItemsByTableIdResponseData();
            data.setTotalPrice(cartItem.getTotalPrice());
            data.setFoodItemList(new ArrayList<>());
            List<CartElementItem> cartElementItemList = cartElementItemRepository.findAllByIdIn(cartItem.getCartElementIds());
            for (CartElementItem cartElementItem : cartElementItemList) {
                RestaurantCartControllerFetchActiveCartItemsByTableIdResponse.RestaurantCartControllerFetchActiveCartItemsByTableIdResponseFoodItem foodItem = new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse.RestaurantCartControllerFetchActiveCartItemsByTableIdResponseFoodItem();
                FoodMenuItem foodMenuItem = restaurantFoodMenuService.getFoodMenuItemById(cartElementItem.getFoodItemId());
                if (foodMenuItem == null) {
                    logger.error("FoodMenuItem not found!!", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), new RuntimeException("FoodMenuItem not found!!"), Map.of("tableId", tableId.toString(), "foodMenuItemId", cartElementItem.getFoodItemId().toString()));
                    throw new RuntimeException("FoodMenuItem not found!!");
                }
                foodItem.setFoodItemPrice(foodMenuItem.getPrice());
                foodItem.setQuantity(cartElementItem.getQuantity());
                foodItem.setFoodItemName(foodMenuItem.getName());
                data.getFoodItemList().add(foodItem);
            }
            return new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse().getSuccessResponse(data, "fetchActiveCartItemsByTableId processed successfully!!");
        } catch (Exception e) {
            logger.error("fetchActiveCartItemsByTableId called!!", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new RestaurantCartControllerFetchActiveCartItemsByTableIdResponse().getInternalServerErrorResponse("Internal server error", e);
        }
    }

    public BaseDBOperationsResponse discardCartByTableId(Long tableId) {
        try {
            logger.info("discardCartByTableId called!!", "discardCartByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
            TableItem tableItem = restaurantTableService.fetchTableById(tableId);
            if (tableItem == null) {
                logger.error("discardCartByTableId called!!", "discardCartByTableId", RestaurantCartService.class.toString(), new RuntimeException("Table not found!!"), Map.of("tableId", tableId.toString()));
                throw new RuntimeException("Table not found!!");
            }
            if (tableItem.getStatus() != TableItemStatusEnum.BOOKED) {
                logger.info("Table not in booked state!!", "discardCartByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("Table not in booked state.So, add items can't be Removed from Cart.");
            }
            CartItem cartItem = cartItemRepository.findByTableIdAndStatus(tableId, CartItemStatusEnum.ACTIVE);
            if (cartItem == null) {
                logger.info("No active CartItem found", "fetchActiveCartItemsByTableId", RestaurantCartService.class.toString(), Map.of("tableId", tableId.toString()));
                return new BaseDBOperationsResponse().getNotAcceptableServerErrorResponse("No active CartItem found");
            }
            cartItem.setStatus(CartItemStatusEnum.IN_ACTIVE);
            cartItemRepository.save(cartItem);
            return new BaseDBOperationsResponse().getSuccessResponse(new BaseDBOperationsResponse.BaseDBOperationsResponseResponseData(), "discardCartByTableId successfully processed.");
        } catch (Exception e) {
            logger.error("discardCartByTableId called!!", "discardCartByTableId", RestaurantCartService.class.toString(), e, Map.of("tableId", tableId.toString()));
            return new BaseDBOperationsResponse().getInternalServerErrorResponse("Internal server error!!", e);
        }
    }

}
