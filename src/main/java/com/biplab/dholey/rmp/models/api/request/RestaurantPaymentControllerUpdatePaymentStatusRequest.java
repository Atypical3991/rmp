package com.biplab.dholey.rmp.models.api.request;

import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantPaymentControllerUpdatePaymentStatusRequest {
    @Positive(message = "billId should be greater than 0.")
    @JsonProperty("billId")
    private Long billId;

    @NotNull(message = "status shouldn't be null.")
    @JsonProperty("status")
    private BillItemStatusEnum status;
}
