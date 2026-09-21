package com.rishikesan.retailanalyticsengine.ingestion;

import lombok.Data;

@Data
public class RejectedRecord {
    private RawOrderRecord record;
    private String reason;

    public RejectedRecord(RawOrderRecord record, String reason) {
        this.record = record;
        this.reason = reason;
    }
}