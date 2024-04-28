package com.biplab.dholey.rmp.repositories;

import com.biplab.dholey.rmp.models.db.OrderItem;
import com.biplab.dholey.rmp.models.db.enums.OrderItemStatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    OrderItem findByIdAndStatus(Long id, OrderItemStatusEnum status);

    Long countByStatus(OrderItemStatusEnum status);

    List<OrderItem> findAllByIdIn(List<Long> orderIds);

    List<OrderItem> findAllByStatusIn(List<OrderItemStatusEnum> statuses);

    List<OrderItem> findAllByTableItemIdAndStatusNot(Long tableId, OrderItemStatusEnum status);
}