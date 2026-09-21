# Retail Analytics Engine

End-to-end retail analytics engine built with Java and SQL that cleans messy sales data and answers real business questions like revenue trends, top sellers, and churn risk through a REST API.

Built with **Java, Spring Boot, MySQL, and raw SQL (window functions, ranking, correlated subqueries)**.

---

## The problem

Raw sales data from real-world sources is rarely clean - duplicate records, invalid emails, negative quantities, inconsistent date formats. Before a business can trust any dashboard or report built on top of it, that data needs to be validated and normalized.

This project simulates that pipeline end-to-end: a messy CSV goes in, a clean relational database comes out, and a set of analytics endpoints answer real business questions - revenue trends, best-selling products, and customers at risk of churning.

---

## Architecture

```
CSV (messy) → Java validation/cleaning → MySQL (normalized schema) → SQL analytics → REST API
```

| Layer | Tool | Why |
|---|---|---|
| Ingestion | Java (CsvParser, RecordValidator, DataLoader) | Row-by-row validation and cleaning needs real logic - not something SQL alone can express well |
| Persistence | Spring Data JPA + MySQL | Standard CRUD, entity relationships, foreign keys |
| Analytics | `JdbcTemplate` + raw SQL | Window functions and correlated subqueries aren't cleanly expressible through an ORM - dropping to raw SQL here shows the actual query design |
| API | Spring Boot REST controllers | Exposes both ingestion and analytics as endpoints |

---

## Schema

```
customers (customer_id, name, email, signup_date, region)
products  (product_id, name, category, unit_price)
orders    (order_id, customer_id, order_date, status)
order_items (order_item_id, order_id, product_id, quantity, unit_price_at_sale)
```

Foreign keys enforce referential integrity between all four tables. `email` on `customers` has a unique constraint.

---

## Data cleaning (Java)

The ingestion pipeline (`/api/ingest`) reads a raw CSV and rejects rows that fail validation, with a specific reason logged for each:

- Missing required fields (customer name, email, product name)
- Invalid email format
- Non-positive quantities (zero or negative)
- Unparseable or negative prices
- Unparseable dates (the parser accepts multiple formats: `2024-01-05`, `01/06/2024`, `Jan 6 2024`)
- Exact duplicate rows

Rows that pass validation are normalized (trimmed, lowercased emails, parsed dates/prices) and loaded via an upsert pattern - existing customers/products are matched by email/name rather than duplicated.

**Example result on the sample dataset (25 rows):** 19 accepted, 6 rejected - each rejection with a human-readable reason, e.g. `"Quantity must be positive (got -2)"`.

---

## Analytics queries (SQL)

Three endpoints, each answering a specific business question:

**`GET /api/analytics/revenue-trend`** - Monthly revenue with a running total, using a window function:
```sql
SELECT
    DATE_FORMAT(o.order_date, '%Y-%m') AS month,
    SUM(oi.quantity * oi.unit_price_at_sale) AS revenue,
    SUM(SUM(oi.quantity * oi.unit_price_at_sale))
        OVER (ORDER BY DATE_FORMAT(o.order_date, '%Y-%m')) AS running_total
FROM orders o
JOIN order_items oi ON o.order_id = oi.order_id
GROUP BY DATE_FORMAT(o.order_date, '%Y-%m')
ORDER BY month
```
*Answers: "Is revenue growing month over month?"*

**`GET /api/analytics/top-products`** - Top 3 best sellers per category, using `RANK()`:
```sql
SELECT category, product_name, units_sold, rnk FROM (
    SELECT p.category, p.name AS product_name, SUM(oi.quantity) AS units_sold,
        RANK() OVER (PARTITION BY p.category ORDER BY SUM(oi.quantity) DESC) AS rnk
    FROM order_items oi JOIN products p ON oi.product_id = p.product_id
    GROUP BY p.category, p.name
) ranked WHERE rnk <= 3
```
*Answers: "What should we stock more of, per category?"*

**`GET /api/analytics/churn-risk?days=14`** - Customers inactive relative to the most recent order in the dataset:
```sql
SELECT c.customer_id, c.name, c.email, MAX(o.order_date) AS last_order_date,
    DATEDIFF((SELECT MAX(order_date) FROM orders), MAX(o.order_date)) AS days_since_last_order
FROM customers c JOIN orders o ON c.customer_id = o.customer_id
GROUP BY c.customer_id, c.name, c.email
HAVING days_since_last_order > ?
ORDER BY days_since_last_order DESC
```
*Answers: "Which customers should we re-engage?"* Uses a correlated subquery against the dataset's own max date, so it works correctly on historical data - not just live data compared to today.

---

## Running it locally

**Prerequisites:** Java 17+, Maven, MySQL 8+

1. Create the database:
   ```sql
   CREATE DATABASE retail_analytics;
   ```
2. Set your MySQL password as an environment variable (kept out of version control):
   ```bash
   export DB_PASSWORD=your_mysql_password
   ```
3. Run the app:
   ```bash
   ./mvnw spring-boot:run
   ```
4. Trigger ingestion (loads the bundled sample CSV):
   ```
   GET http://localhost:8080/api/ingest
   ```
5. Query the analytics endpoints:
   ```
   GET http://localhost:8080/api/analytics/revenue-trend
   GET http://localhost:8080/api/analytics/top-products
   GET http://localhost:8080/api/analytics/churn-risk?days=14
   ```

---

## Design decisions worth noting

- **JPA for CRUD, raw SQL for analytics** - used the right tool for each job rather than forcing everything through one abstraction.
- **`BigDecimal` for money, not `double`** - avoids floating-point rounding errors in financial calculations.
- **Environment variables for credentials** - `application.properties` references `${DB_PASSWORD}` rather than a hardcoded value, so secrets never enter version control.
- **Upsert pattern for customers/products** - the ingestion pipeline checks for existing records before creating new ones, so re-running ingestion on overlapping data doesn't create duplicates.

---

## Extending to other domains

The schema and pipeline pattern generalize directly to adjacent use cases - for example, a portfolio/finance version:

```
investors (investor_id, name, email, signup_date, region)
assets    (asset_id, ticker, sector, current_price)
trades    (trade_id, investor_id, trade_date, status)
trade_items (trade_item_id, trade_id, asset_id, quantity, price_at_trade)
```

Same joins, same window functions - swapped domain, identical technical approach (portfolio value trends, top holdings by sector, investor inactivity).
