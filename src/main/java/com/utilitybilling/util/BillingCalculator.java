package com.utilitybilling.util;

import com.utilitybilling.entity.TariffTier;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;

public final class BillingCalculator {

    private BillingCalculator() {}

    public record ChargeBreakdown(BigDecimal subtotal, BigDecimal taxAmount, BigDecimal totalAmount) {}

    public static ChargeBreakdown calculate(
            BigDecimal consumption,
            BigDecimal ratePerUnit,
            BigDecimal fixedCharge,
            BigDecimal vatPercentage) {

        BigDecimal subtotal = consumption.multiply(ratePerUnit).setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(fixedCharge).add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        return new ChargeBreakdown(subtotal, taxAmount, totalAmount);
    }

    public static ChargeBreakdown calculateWithTiers(
            BigDecimal consumption,
            List<TariffTier> tiers,
            BigDecimal fixedCharge,
            BigDecimal vatPercentage) {

        if (tiers == null || tiers.isEmpty()) {
            throw new IllegalArgumentException("Tariff tiers are required for tiered billing");
        }

        BigDecimal remaining = consumption;
        BigDecimal subtotal = BigDecimal.ZERO;

        List<TariffTier> sorted = tiers.stream()
                .sorted(Comparator.comparing(TariffTier::getMinUnits))
                .toList();

        for (TariffTier tier : sorted) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierSpan = tier.getMaxUnits().subtract(tier.getMinUnits());
            BigDecimal unitsInTier = remaining.min(tierSpan);
            if (unitsInTier.compareTo(BigDecimal.ZERO) > 0) {
                subtotal = subtotal.add(unitsInTier.multiply(tier.getRatePerUnit()));
                remaining = remaining.subtract(unitsInTier);
            }
        }

        subtotal = subtotal.setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = subtotal.multiply(vatPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(fixedCharge).add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        return new ChargeBreakdown(subtotal, taxAmount, totalAmount);
    }

    public static BigDecimal calculatePenalty(BigDecimal totalAmount, BigDecimal penaltyPercentage) {
        return totalAmount.multiply(penaltyPercentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
