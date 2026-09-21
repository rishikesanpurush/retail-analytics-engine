package com.rishikesan.retailanalyticsengine.controller;

import com.rishikesan.retailanalyticsengine.analytics.AnalyticsService;
import com.rishikesan.retailanalyticsengine.analytics.ChurnRiskRow;
import com.rishikesan.retailanalyticsengine.analytics.RevenueTrendRow;
import com.rishikesan.retailanalyticsengine.analytics.TopProductRow;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/revenue-trend")
    public List<RevenueTrendRow> revenueTrend() {
        return analyticsService.getRevenueTrend();
    }

    @GetMapping("/top-products")
    public List<TopProductRow> topProducts() {
        return analyticsService.getTopProductsByCategory();
    }

    @GetMapping("/churn-risk")
    public List<ChurnRiskRow> churnRisk(@RequestParam(defaultValue = "14") int days) {
        return analyticsService.getChurnRisk(days);
    }
}