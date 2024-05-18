package com.biplab.dholey.rmp.common.constant;

import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;

import java.util.Map;

public class EnumToTextMap {

    public static Map<BillItemStatusEnum,String> BILL_STATUS_TO_TEXT_MAP = Map.of(
            BillItemStatusEnum.PAYMENT_SUCCESS, "Payment received successfully.",
            BillItemStatusEnum.PAYMENT_INITIATED, "Payment has been initiated, please complete the payment.",
            BillItemStatusEnum.PAYMENT_FAILED, "Payment failed, please make the payment again.",
            BillItemStatusEnum.BILL_GENERATED, "Payment hasn't been initiated yet."
    );
}
