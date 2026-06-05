package com.utilitybilling.service;

import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.User;
import com.utilitybilling.enums.RoleName;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerAccessService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    public boolean isStaff(String userEmail) {
        return findUser(userEmail).getRoles().stream()
                .anyMatch(r -> r.getName() != RoleName.ROLE_CUSTOMER);
    }

    public void assertStaffOrOwnCustomer(String userEmail, Long customerId) {
        if (isStaff(userEmail)) {
            return;
        }
        Long ownId = requireOwnCustomerId(userEmail);
        if (!ownId.equals(customerId)) {
            throw new BusinessRuleException("Access denied: customers may only view their own records");
        }
    }

    public Long requireOwnCustomerId(String userEmail) {
        User user = findUser(userEmail);
        return customerRepository.findByUserId(user.getId())
                .map(Customer::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found for user"));
    }

    private User findUser(String userEmail) {
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
