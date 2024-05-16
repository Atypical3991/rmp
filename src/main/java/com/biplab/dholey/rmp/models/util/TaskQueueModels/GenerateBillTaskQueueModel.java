package com.biplab.dholey.rmp.models.util.TaskQueueModels;


import lombok.Data;

@Data
public class GenerateBillTaskQueueModel implements TaskQueueInterface {
    Long billItemId;
    Long tableId;

    public GenerateBillTaskQueueModel(Long billItemId, Long tableId) {
        this.billItemId = billItemId;
        this.tableId = tableId;
    }
}
