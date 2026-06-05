package com.utilitybilling.entity;

import com.utilitybilling.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class User extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 75)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 75)
    private String lastName;

    @Column(name = "full_names", nullable = false, length = 150)
    private String fullNames;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "phone_country_code", nullable = false, length = 5)
    @Builder.Default
    private String phoneCountryCode = "+250";

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "password_expired", nullable = false)
    @Builder.Default
    private Boolean passwordExpired = false;

    @Column(name = "account_locked", nullable = false)
    @Builder.Default
    private Boolean accountLocked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
