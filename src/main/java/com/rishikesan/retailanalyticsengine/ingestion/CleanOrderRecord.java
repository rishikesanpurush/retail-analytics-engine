package com.rishikesan.retailanalyticsengine.ingestion;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CleanOrderRecord {
    private String customerName;
    private String customerEmail;
    private String productName;
    private String category;
    private BigDecimal unitPrice;
    private Integer quantity;
    private LocalDate orderDate;
    private String status;
}