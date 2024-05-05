package com.biplab.dholey.rmp.models.util;


import lombok.Data;

import java.util.List;

@Data
public class GenerateBillTaskQueueModel {
    List<Long> orderItemIds;
    Long tableId;
}
