package com.rishikesan.retailanalyticsengine.analytics;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RevenueTrendRow {
    private String month;
    private BigDecimal revenue;
    private BigDecimal runningTotal;
}