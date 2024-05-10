package com.biplab.dholey.rmp.models.util.TaskQueueModels;

import lombok.Data;

@Data
public class TableCleanRequestTaskQueueModel implements TaskQueueInterface {
    private Long tableId;
}
