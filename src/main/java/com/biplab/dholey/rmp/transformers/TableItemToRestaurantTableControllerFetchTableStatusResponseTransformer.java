package com.biplab.dholey.rmp.transformers;

import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.db.TableItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TableItemToRestaurantTableControllerFetchTableStatusResponseTransformer implements Transformer<List<TableItem>, RestaurantTableControllerFetchAllAvailableTablesResponse> {
    @Override
    public RestaurantTableControllerFetchAllAvailableTablesResponse transform(List<TableItem> source) {
        RestaurantTableControllerFetchAllAvailableTablesResponse response = new RestaurantTableControllerFetchAllAvailableTablesResponse();
        List<RestaurantTableControllerFetchAllAvailableTablesResponse.RestaurantTableControllerFetchAllAvailableTablesResponseResponseData.Table> tablesList = new ArrayList<>();
        for (TableItem tableItem : source) {
            RestaurantTableControllerFetchAllAvailableTablesResponse.RestaurantTableControllerFetchAllAvailableTablesResponseResponseData.Table table = new RestaurantTableControllerFetchAllAvailableTablesResponse.RestaurantTableControllerFetchAllAvailableTablesResponseResponseData.Table();
            table.setNumber(tableItem.getTableNumber());
            table.setOccupancy(tableItem.getOccupancy());
            table.setStatus(tableItem.getStatus().name());
            tablesList.add(table);
        }
        response.setData(new RestaurantTableControllerFetchAllAvailableTablesResponse.RestaurantTableControllerFetchAllAvailableTablesResponseResponseData());
        response.getData().setTablesList(tablesList);
        return response;
    }
}
