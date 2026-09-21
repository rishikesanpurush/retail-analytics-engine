package com.rishikesan.retailanalyticsengine.ingestion;

import lombok.Data;

@Data
public class RawOrderRecord {
    private String customerName;
    private String customerEmail;
    private String productName;
    private String category;
    private String unitPriceRaw;
    private String quantityRaw;
    private String orderDateRaw;
    private String statusRaw;
    private int sourceLineNumber; // for error reporting
}