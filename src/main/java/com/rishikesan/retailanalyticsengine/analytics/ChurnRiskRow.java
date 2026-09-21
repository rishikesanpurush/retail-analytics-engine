package com.rishikesan.retailanalyticsengine.analytics;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ChurnRiskRow {
    private Long customerId;
    private String name;
    private String email;
    private LocalDate lastOrderDate;
    private long daysSinceLastOrder;
}