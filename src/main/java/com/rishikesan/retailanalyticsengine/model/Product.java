package com.rishikesan.retailanalyticsengine.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Data

public class Product {
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private String name;

    private String category;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

}
