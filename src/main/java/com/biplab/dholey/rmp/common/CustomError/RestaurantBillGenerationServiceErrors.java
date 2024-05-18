package com.biplab.dholey.rmp.common.CustomError;

public class RestaurantBillGenerationServiceErrors {

    public static String GENERATE_BILL_EMPTY_ORDERS_LIST_IN_REQUEST_ERROR = "Empty orders list in request can't be processed.";
    public static String GENERATE_BILL_NO_UN_BILLED_ORDER_FOUND_ERROR = "No un-billed order found.";

    public static String GENERATE_BILL_FOOD_MENU_ITEM_NOT_FOUND_ERROR = "Food menu item not found for a given order.";

    public static String GENERATE_UPDATE_BILL_GENERATION_STATUS_FAILED_ERROR = "updateBillGenerationStatus failed.";

    public static String GENERATE_UPDATE_BILL_GENERATED_AT_FAILED_ERROR = "updateBillGenerateAt failed.";
}

