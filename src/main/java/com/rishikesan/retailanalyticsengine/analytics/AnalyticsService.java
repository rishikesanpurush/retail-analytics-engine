package com.rishikesan.retailanalyticsengine.analytics;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    public List<RevenueTrendRow> getRevenueTrend() {
        return analyticsRepository.getRevenueTrend();
    }

    public List<TopProductRow> getTopProductsByCategory() {
        return analyticsRepository.getTopProductsByCategory();
    }

    public List<ChurnRiskRow> getChurnRisk(int inactivityThresholdDays) {
        return analyticsRepository.getChurnRisk(inactivityThresholdDays);
    }
}