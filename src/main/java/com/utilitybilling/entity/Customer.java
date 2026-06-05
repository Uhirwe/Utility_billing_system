package com.utilitybilling.entity;

import com.utilitybilling.enums.CustomerStatus;
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
@Table(name = "customers")
public class Customer extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Column(name = "full_names", nullable = false, length = 150)
    private String fullNames;

    @Column(name = "national_id", nullable = false, unique = true, length = 16)
    private String nationalId;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_country_code", nullable = false, length = 5)
    @Builder.Default
    private String phoneCountryCode = "+250";

    @Column(name = "phone_number", nullable = false, unique = true, length = 20)
    private String phoneNumber;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Meter> meters = new ArrayList<>();
}
