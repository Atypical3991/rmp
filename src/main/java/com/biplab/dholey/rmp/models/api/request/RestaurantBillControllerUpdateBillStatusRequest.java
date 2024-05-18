package com.biplab.dholey.rmp.models.api.request;

import com.biplab.dholey.rmp.models.db.enums.BillItemStatusEnum;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RestaurantBillControllerUpdateBillStatusRequest {

    @Positive(message = "billId should be greater than 0.")
    @JsonProperty("billId")
    private Long billId;
    @NotBlank(message = "status shouldn't be blank.")
    @JsonProperty("status")
    private BillItemStatusEnum status;
}
