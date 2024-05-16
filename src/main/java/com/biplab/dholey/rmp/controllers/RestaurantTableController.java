package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllBookedTablesResponse;
import com.biplab.dholey.rmp.services.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-table")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService restaurantTableService;

    @GetMapping("/fetch-available-tables")
    public ResponseEntity<RestaurantTableControllerFetchAllAvailableTablesResponse> fetchAvailableTables() {
        return ResponseEntity.ok().body(restaurantTableService.fetchAllAvailableTables());
    }

    @GetMapping("/fetch-booked-tables")
    public ResponseEntity<RestaurantTableControllerFetchAllBookedTablesResponse> fetchBookedTables() {
        return ResponseEntity.ok().body(restaurantTableService.fetchBookedTables());
    }

    @PutMapping("/un-book-table")
    public ResponseEntity<BaseDBOperationsResponse> unBookTable(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantTableService.unBookTable(tableId));
    }

    @PutMapping("/book-by-table-id")
    public ResponseEntity<BaseDBOperationsResponse> bookTableByTableId(@RequestParam(value = "tableId") Long occupancy) {
        return ResponseEntity.ok().body(restaurantTableService.bookTableByTableId(occupancy));
    }

    @PostMapping("/add-table")
    public ResponseEntity<BaseDBOperationsResponse> addTable(@RequestBody RestaurantTableControllerAddTableRequest addTableRequest) {
        return ResponseEntity.ok().body(restaurantTableService.addTable(addTableRequest));
    }

    @PutMapping("/remove-table")
    public ResponseEntity<BaseDBOperationsResponse> removeTable(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantTableService.removeTable(tableId));
    }

    @PutMapping("/clean-table")
    public ResponseEntity<BaseDBOperationsResponse> cleanTable(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantTableService.cleanTable(tableId));
    }

}
