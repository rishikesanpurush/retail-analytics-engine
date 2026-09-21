package com.rishikesan.retailanalyticsengine.ingestion;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class ValidationResult {
    private List<CleanOrderRecord> cleanRecords = new ArrayList<>();
    private List<RejectedRecord> rejectedRecords = new ArrayList<>();
}