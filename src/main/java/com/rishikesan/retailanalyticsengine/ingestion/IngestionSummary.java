package com.rishikesan.retailanalyticsengine.ingestion;

import lombok.Data;
import java.util.List;

@Data
public class IngestionSummary {
    private int totalRowsParsed;
    private int rowsAccepted;
    private int rowsRejected;
    private List<RejectedRecord> rejectedDetails;
}