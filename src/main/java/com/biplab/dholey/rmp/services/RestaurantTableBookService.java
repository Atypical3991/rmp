package com.biplab.dholey.rmp.services;

import com.biplab.dholey.rmp.models.db.TableBookedItem;
import com.biplab.dholey.rmp.repositories.TableBookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RestaurantTableBookService {

    @Autowired
    private TableBookRepository tableBookRepository;

    public Boolean updateLastOrderServedAt(Long tableId) {
        try {
            TableBookedItem tableBookedItem = tableBookRepository.findByTableId(tableId);
            tableBookedItem.setLastOrderServedAt(LocalDateTime.now());
            tableBookRepository.save(tableBookedItem);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
