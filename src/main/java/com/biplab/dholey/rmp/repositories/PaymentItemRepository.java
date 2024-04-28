package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.PaymentItem;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
}