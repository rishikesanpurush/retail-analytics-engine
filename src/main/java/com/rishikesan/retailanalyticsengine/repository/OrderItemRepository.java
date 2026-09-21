package com.rishikesan.retailanalyticsengine.repository;

import com.rishikesan.retailanalyticsengine.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}