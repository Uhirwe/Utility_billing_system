package com.utilitybilling.mapper;

import com.utilitybilling.dto.customer.CustomerRequest;
import com.utilitybilling.dto.customer.CustomerResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.enums.CustomerStatus;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public Customer toEntity(CustomerRequest request) {
        return Customer.builder()
                .fullNames(request.getFullNames())
                .nationalId(request.getNationalId())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .dateOfBirth(request.getDateOfBirth())
                .status(CustomerStatus.ACTIVE)
                .build();
    }

    public void updateEntity(Customer customer, CustomerRequest request) {
        customer.setFullNames(request.getFullNames());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        customer.setDateOfBirth(request.getDateOfBirth());
    }

    public CustomerResponse toResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullNames(customer.getFullNames())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneCountryCode(customer.getPhoneCountryCode())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .dateOfBirth(customer.getDateOfBirth())
                .status(customer.getStatus())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
