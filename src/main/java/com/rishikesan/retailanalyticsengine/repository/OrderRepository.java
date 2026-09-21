package com.rishikesan.retailanalyticsengine.repository;

import com.rishikesan.retailanalyticsengine.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}