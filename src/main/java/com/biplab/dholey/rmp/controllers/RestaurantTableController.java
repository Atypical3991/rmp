package com.biplab.dholey.rmp.controllers;

import com.biplab.dholey.rmp.models.api.request.RestaurantTableControllerAddTableRequest;
import com.biplab.dholey.rmp.models.api.response.BaseDBOperationsResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchAllAvailableTablesResponse;
import com.biplab.dholey.rmp.models.api.response.RestaurantTableControllerFetchTableStatusResponse;
import com.biplab.dholey.rmp.services.RestaurantTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurant-table")
public class RestaurantTableController {

    @Autowired
    private RestaurantTableService restaurantTableService;

    @GetMapping("/available-tables")
    public ResponseEntity<RestaurantTableControllerFetchAllAvailableTablesResponse> fetchAvailableTables() {
        return ResponseEntity.ok().body(restaurantTableService.fetchAllTables());
    }

    @PostMapping("/add-table")
    public ResponseEntity<BaseDBOperationsResponse> addTable(@RequestBody RestaurantTableControllerAddTableRequest addTableRequest) {
        return ResponseEntity.ok().body(restaurantTableService.addTable(addTableRequest));
    }

    @PutMapping("/remove-table")
    public ResponseEntity<BaseDBOperationsResponse> removeTable(@RequestParam(value = "tableNumber") Long tableNumber) {
        return ResponseEntity.ok().body(restaurantTableService.removeTable(tableNumber));
    }

    @GetMapping("/fetch-all-tables")
    public ResponseEntity<RestaurantTableControllerFetchTableStatusResponse> fetchTableStatus(@RequestParam(value = "tableId") Long tableNumber) {
        return ResponseEntity.ok().body(restaurantTableService.fetchTableStatus(tableNumber));
    }

    @PutMapping("/book-table")
    public ResponseEntity<BaseDBOperationsResponse> bookTable(@RequestParam(value = "tableNumber") Long tableNumber) {
        return ResponseEntity.ok().body(restaurantTableService.bookTable(tableNumber));
    }

    @PutMapping("/book-table-by-occupancy")
    public ResponseEntity<BaseDBOperationsResponse> bookTableByOccupancy(@RequestParam(value = "occupancy") Long occupancy) {
        return ResponseEntity.ok().body(restaurantTableService.bookTableByOccupancy(occupancy));
    }


    @PutMapping("/un-book-table")
    public ResponseEntity<BaseDBOperationsResponse> unBookTable(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantTableService.unBookTable(tableId));
    }

    @PutMapping("/clean-table")
    public ResponseEntity<BaseDBOperationsResponse> cleanTable(@RequestParam(value = "tableId") Long tableId) {
        return ResponseEntity.ok().body(restaurantTableService.unBookTable(tableId));
    }

}
