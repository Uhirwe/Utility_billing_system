package com.utilitybilling.entity;

import com.utilitybilling.enums.BillingMode;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.enums.MeterType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "meters")
public class Meter extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "meter_number", nullable = false, unique = true, length = 50)
    private String meterNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "meter_type", nullable = false, length = 20)
    private MeterType meterType;

    @Column(name = "installation_date", nullable = false)
    private LocalDate installationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private MeterStatus status = MeterStatus.ACTIVE;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_mode", nullable = false, length = 20)
    @Builder.Default
    private BillingMode billingMode = BillingMode.POSTPAID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @OneToMany(mappedBy = "meter", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MeterReading> readings = new ArrayList<>();
}
