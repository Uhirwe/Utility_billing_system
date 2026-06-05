package com.utilitybilling.entity;

import com.utilitybilling.enums.MeterType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Versioned tariff configuration for utility billing calculations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tariffs", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meter_type", "version_number"})
})
public class Tariff extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Column(name = "tariff_name", nullable = false, length = 100)
    private String tariffName;

    @Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal ratePerUnit;

    @Column(name = "fixed_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal fixedCharge;

    @Column(name = "vat_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal vatPercentage;

    @Column(name = "late_penalty_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal latePenaltyPercentage;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber;

    @Column(name = "effective_date", nullable = false)
    private LocalDate effectiveDate;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "tariff", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TariffTier> tiers = new ArrayList<>();
}
