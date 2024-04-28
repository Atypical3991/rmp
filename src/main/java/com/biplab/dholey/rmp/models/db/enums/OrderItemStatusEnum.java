package com.biplab.dholey.rmp.models.db.enums;

public enum OrderItemStatusEnum {
    QUEUED,
    PICKED_BY_COOK,
    READY_TO_SERVE,
    SERVED,
    BILL_GENERATED,
    PAYMENT_INITIATED,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,
    COMPLETED
}
