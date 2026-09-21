package com.rishikesan.retailanalyticsengine.ingestion;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Component
public class RecordValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$");

    private static final List<DateTimeFormatter> DATE_FORMATS = List.of(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),
            DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH)
    );

    public ValidationResult validate(List<RawOrderRecord> rawRecords) {
        ValidationResult result = new ValidationResult();
        Set<String> seenKeys = new HashSet<>();

        for (RawOrderRecord raw : rawRecords) {
            String reason = findRejectionReason(raw);

            if (reason != null) {
                result.getRejectedRecords().add(new RejectedRecord(raw, reason));
                continue;
            }

            CleanOrderRecord clean = normalize(raw);

            String dedupeKey = clean.getCustomerEmail() + "|" + clean.getProductName() + "|"
                    + clean.getOrderDate() + "|" + clean.getQuantity() + "|" + clean.getUnitPrice();

            if (seenKeys.contains(dedupeKey)) {
                result.getRejectedRecords().add(new RejectedRecord(raw, "Duplicate of an earlier row"));
                continue;
            }
            seenKeys.add(dedupeKey);

            result.getCleanRecords().add(clean);
        }

        return result;
    }

    private String findRejectionReason(RawOrderRecord raw) {
        if (isBlank(raw.getCustomerName())) return "Missing customer name";
        if (isBlank(raw.getCustomerEmail())) return "Missing customer email";
        if (!EMAIL_PATTERN.matcher(raw.getCustomerEmail().trim()).matches()) return "Invalid email format";
        if (isBlank(raw.getProductName())) return "Missing product name";

        Integer quantity = tryParseQuantity(raw.getQuantityRaw());
        if (quantity == null) return "Quantity is not a valid number";
        if (quantity <= 0) return "Quantity must be positive (got " + quantity + ")";

        BigDecimal price = tryParsePrice(raw.getUnitPriceRaw());
        if (price == null) return "Unit price is not a valid number";
        if (price.compareTo(BigDecimal.ZERO) < 0) return "Unit price cannot be negative";

        LocalDate date = tryParseDate(raw.getOrderDateRaw());
        if (date == null) return "Order date could not be parsed";

        return null;
    }

    private CleanOrderRecord normalize(RawOrderRecord raw) {
        CleanOrderRecord clean = new CleanOrderRecord();
        clean.setCustomerName(raw.getCustomerName().trim());
        clean.setCustomerEmail(raw.getCustomerEmail().trim().toLowerCase(Locale.ROOT));
        clean.setProductName(raw.getProductName().trim());
        clean.setCategory(isBlank(raw.getCategory()) ? "Uncategorized" : raw.getCategory().trim());
        clean.setUnitPrice(tryParsePrice(raw.getUnitPriceRaw()));
        clean.setQuantity(tryParseQuantity(raw.getQuantityRaw()));
        clean.setOrderDate(tryParseDate(raw.getOrderDateRaw()));
        clean.setStatus(raw.getStatusRaw().trim().toLowerCase(Locale.ROOT));
        return clean;
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private Integer tryParseQuantity(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal tryParsePrice(String raw) {
        try {
            String cleaned = raw.trim().replace("$", "");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate tryParseDate(String raw) {
        String trimmed = raw.trim();
        for (DateTimeFormatter fmt : DATE_FORMATS) {
            try {
                return LocalDate.parse(trimmed, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}