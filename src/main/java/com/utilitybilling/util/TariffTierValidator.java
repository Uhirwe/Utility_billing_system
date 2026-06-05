package com.utilitybilling.util;

import com.utilitybilling.dto.tariff.TariffTierRequest;
import com.utilitybilling.exception.ValidationException;

import java.util.Comparator;
import java.util.List;

public final class TariffTierValidator {

    private TariffTierValidator() {}

    public static void validateNonOverlapping(List<TariffTierRequest> tiers) {
        if (tiers == null || tiers.isEmpty()) {
            return;
        }

        List<TariffTierRequest> sorted = tiers.stream()
                .sorted(Comparator.comparing(TariffTierRequest::getMinUnits))
                .toList();

        for (int i = 0; i < sorted.size(); i++) {
            TariffTierRequest tier = sorted.get(i);
            if (tier.getMaxUnits().compareTo(tier.getMinUnits()) <= 0) {
                throw new ValidationException("Tier max units must be greater than min units");
            }
            if (i > 0) {
                TariffTierRequest previous = sorted.get(i - 1);
                if (tier.getMinUnits().compareTo(previous.getMaxUnits()) < 0) {
                    throw new ValidationException("Tariff tiers must not overlap");
                }
            }
        }
    }
}
