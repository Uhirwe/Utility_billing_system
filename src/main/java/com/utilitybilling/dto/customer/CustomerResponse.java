package com.utilitybilling.dto.customer;

import com.utilitybilling.enums.CustomerStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerResponse {
    private Long id;
    private String fullNames;
    private String nationalId;
    private String email;
    private String phoneCountryCode;
    private String phoneNumber;
    private String address;
    private LocalDate dateOfBirth;
    private CustomerStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
