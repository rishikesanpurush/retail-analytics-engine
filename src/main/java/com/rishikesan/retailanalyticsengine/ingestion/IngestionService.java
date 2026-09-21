package com.rishikesan.retailanalyticsengine.ingestion;

import org.springframework.stereotype.Service;
import java.io.InputStream;
import java.util.List;

@Service
public class IngestionService {

    private final CsvParser csvParser;
    private final RecordValidator recordValidator;
    private final DataLoader dataLoader;

    public IngestionService(CsvParser csvParser, RecordValidator recordValidator, DataLoader dataLoader) {
        this.csvParser = csvParser;
        this.recordValidator = recordValidator;
        this.dataLoader = dataLoader;
    }

    public IngestionSummary ingest(InputStream csvInputStream) {
        List<RawOrderRecord> rawRecords = csvParser.parse(csvInputStream);
        ValidationResult validationResult = recordValidator.validate(rawRecords);
        dataLoader.load(validationResult.getCleanRecords());

        IngestionSummary summary = new IngestionSummary();
        summary.setTotalRowsParsed(rawRecords.size());
        summary.setRowsAccepted(validationResult.getCleanRecords().size());
        summary.setRowsRejected(validationResult.getRejectedRecords().size());
        summary.setRejectedDetails(validationResult.getRejectedRecords());
        return summary;
    }
}