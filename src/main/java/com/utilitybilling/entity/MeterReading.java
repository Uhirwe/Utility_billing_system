package com.utilitybilling.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Monthly meter reading with auto-calculated consumption.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "meter_readings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meter_id", "reading_month", "reading_year"})
})
public class MeterReading extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(name = "previous_reading", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal previousReading = BigDecimal.ZERO;

    @Column(name = "current_reading", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentReading;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal consumption = BigDecimal.ZERO;

    @Column(name = "reading_date", nullable = false)
    private LocalDate readingDate;

    @Column(name = "reading_month", nullable = false)
    private Integer readingMonth;

    @Column(name = "reading_year", nullable = false)
    private Integer readingYear;
}
