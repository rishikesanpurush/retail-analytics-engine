package com.rishikesan.retailanalyticsengine.ingestion;

import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Component
public class CsvParser {

    public List<RawOrderRecord> parse(InputStream inputStream) {
        List<RawOrderRecord> records = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                if (isHeader) {
                    isHeader = false; // skip the header row
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue; // skip blank lines
                }

                String[] fields = line.split(",", -1); // -1 keeps trailing empty fields

                if (fields.length < 8) {
                    continue; // malformed row, too few columns — silently skip (could log this)
                }

                RawOrderRecord record = new RawOrderRecord();
                record.setCustomerName(fields[0]);
                record.setCustomerEmail(fields[1]);
                record.setProductName(fields[2]);
                record.setCategory(fields[3]);
                record.setUnitPriceRaw(fields[4]);
                record.setQuantityRaw(fields[5]);
                record.setOrderDateRaw(fields[6]);
                record.setStatusRaw(fields[7]);
                record.setSourceLineNumber(lineNumber);

                records.add(record);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV: " + e.getMessage(), e);
        }

        return records;
    }
}