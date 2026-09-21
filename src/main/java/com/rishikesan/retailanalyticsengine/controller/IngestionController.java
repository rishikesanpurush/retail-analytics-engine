package com.rishikesan.retailanalyticsengine.controller;

import com.rishikesan.retailanalyticsengine.ingestion.IngestionService;
import com.rishikesan.retailanalyticsengine.ingestion.IngestionSummary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("/api/ingest")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @GetMapping
    public IngestionSummary ingestBundledCsv() throws Exception {
        try (InputStream inputStream = new ClassPathResource("data/raw_orders_messy.csv").getInputStream()) {
            return ingestionService.ingest(inputStream);
        }
    }
}