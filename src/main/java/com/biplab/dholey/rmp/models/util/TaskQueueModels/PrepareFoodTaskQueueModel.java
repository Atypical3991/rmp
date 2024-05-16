package com.biplab.dholey.rmp.models.util.TaskQueueModels;

import lombok.Data;

@Data
public class PrepareFoodTaskQueueModel implements TaskQueueInterface {
    private Long orderId;
    private Long tableId;

    public PrepareFoodTaskQueueModel(Long orderId, Long tableId) {
        this.orderId = orderId;
        this.tableId = tableId;
    }
}
