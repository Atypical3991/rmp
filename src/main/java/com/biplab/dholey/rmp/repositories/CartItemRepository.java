package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.CartItem;
import com.biplab.dholey.rmp.models.db.enums.CartItemStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    CartItem findByTableIdAndStatus(Long tableId, CartItemStatusEnum statusEnum);
}
