package com.utilitybilling.service;

import com.utilitybilling.dto.customer.CustomerProfileUpdateRequest;
import com.utilitybilling.dto.customer.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse getMyProfile(String userEmail);
    CustomerResponse updateMyProfile(String userEmail, CustomerProfileUpdateRequest request);
    CustomerResponse getCustomerById(Long id, String requesterEmail);
    Page<CustomerResponse> getAllCustomers(Pageable pageable);
    void activateCustomer(Long id);
    void deactivateCustomer(Long id);
}
