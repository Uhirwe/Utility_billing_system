package com.utilitybilling.util;

import java.time.YearMonth;
import java.util.UUID;

/**
 * Generates unique bill numbers for utility billing.
 */
public final class BillNumberGenerator {

    private BillNumberGenerator() {}

    public static String generate(Long meterId, int month, int year) {
        String period = String.format("%04d%02d", year, month);
        String suffix = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "BILL-" + period + "-" + meterId + "-" + suffix;
    }

    public static String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
