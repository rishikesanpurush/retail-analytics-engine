package com.rishikesan.retailanalyticsengine.analytics;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** Monthly revenue with a running total, using a window function. */
    public List<RevenueTrendRow> getRevenueTrend() {
        String sql = """
            SELECT
                DATE_FORMAT(o.order_date, '%Y-%m') AS month,
                SUM(oi.quantity * oi.unit_price_at_sale) AS revenue,
                SUM(SUM(oi.quantity * oi.unit_price_at_sale))
                    OVER (ORDER BY DATE_FORMAT(o.order_date, '%Y-%m')) AS running_total
            FROM orders o
            JOIN order_items oi ON o.order_id = oi.order_id
            GROUP BY DATE_FORMAT(o.order_date, '%Y-%m')
            ORDER BY month
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            RevenueTrendRow row = new RevenueTrendRow();
            row.setMonth(rs.getString("month"));
            row.setRevenue(rs.getBigDecimal("revenue"));
            row.setRunningTotal(rs.getBigDecimal("running_total"));
            return row;
        });
    }

    /** Top 3 best-selling products per category, using RANK(). */
    public List<TopProductRow> getTopProductsByCategory() {
        String sql = """
            SELECT category, product_name, units_sold, rnk
            FROM (
                SELECT
                    p.category,
                    p.name AS product_name,
                    SUM(oi.quantity) AS units_sold,
                    RANK() OVER (PARTITION BY p.category ORDER BY SUM(oi.quantity) DESC) AS rnk
                FROM order_items oi
                JOIN products p ON oi.product_id = p.product_id
                GROUP BY p.category, p.name
            ) ranked
            WHERE rnk <= 3
            ORDER BY category, rnk
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            TopProductRow row = new TopProductRow();
            row.setCategory(rs.getString("category"));
            row.setProductName(rs.getString("product_name"));
            row.setUnitsSold(rs.getLong("units_sold"));
            row.setRank(rs.getInt("rnk"));
            return row;
        });
    }

    /** Customers who haven't ordered recently, relative to the most recent order in the dataset. */
    public List<ChurnRiskRow> getChurnRisk(int inactivityThresholdDays) {
        String sql = """
            SELECT
                c.customer_id,
                c.name,
                c.email,
                MAX(o.order_date) AS last_order_date,
                DATEDIFF((SELECT MAX(order_date) FROM orders), MAX(o.order_date)) AS days_since_last_order
            FROM customers c
            JOIN orders o ON c.customer_id = o.customer_id
            GROUP BY c.customer_id, c.name, c.email
            HAVING days_since_last_order > ?
            ORDER BY days_since_last_order DESC
            """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ChurnRiskRow row = new ChurnRiskRow();
            row.setCustomerId(rs.getLong("customer_id"));
            row.setName(rs.getString("name"));
            row.setEmail(rs.getString("email"));
            row.setLastOrderDate(rs.getDate("last_order_date").toLocalDate());
            row.setDaysSinceLastOrder(rs.getLong("days_since_last_order"));
            return row;
        }, inactivityThresholdDays);
    }
}