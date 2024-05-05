package com.biplab.dholey.rmp.models.util;

import lombok.Data;

@Data
public class TableCleanRequestTaskQueueModel implements TaskQueueInterface {
    private Long tableId;
}
