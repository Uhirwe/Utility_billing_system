package com.utilitybilling.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tariff_tiers")
public class TariffTier extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(name = "tier_name", nullable = false, length = 100)
    private String tierName;

    @Column(name = "min_units", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minUnits = BigDecimal.ZERO;

    @Column(name = "max_units", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxUnits;

    @Column(name = "rate_per_unit", nullable = false, precision = 12, scale = 4)
    private BigDecimal ratePerUnit;
}
