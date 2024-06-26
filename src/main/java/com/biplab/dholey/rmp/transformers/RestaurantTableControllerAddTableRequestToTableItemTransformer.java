package com.biplab.dholey.rmp.transformers;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.db.TableItem;
import com.biplab.dholey.rmp.models.db.enums.TableItemStatusEnum;
import org.springframework.stereotype.Component;

@Component
public class RestaurantTableControllerAddTableRequestToTableItemTransformer implements TransformerInterface<RestaurantTableControllerAddTableRequest, TableItem> {

    @Override
    public TableItem transform(RestaurantTableControllerAddTableRequest obj) {
        TableItem item = new TableItem();
        item.setTableNumber(obj.getTableNumber());
        item.setOccupancy(obj.getTableOccupancy());
        item.setStatus(TableItemStatusEnum.AVAILABLE);
        return item;
    }
}
