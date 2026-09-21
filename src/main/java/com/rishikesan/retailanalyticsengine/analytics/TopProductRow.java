package com.rishikesan.retailanalyticsengine.analytics;

import lombok.Data;

@Data
public class TopProductRow {
    private String category;
    private String productName;
    private long unitsSold;
    private int rank;
}