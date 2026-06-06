package com.utilitybilling.service.impl;

import com.utilitybilling.dto.customer.CustomerProfileUpdateRequest;
import com.utilitybilling.dto.customer.CustomerResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.User;
import com.utilitybilling.enums.CustomerStatus;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.CustomerMapper;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.UserRepository;
import com.utilitybilling.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getMyProfile(String userEmail) {
        return customerMapper.toResponse(findCustomerByUserEmail(userEmail));
    }

    @Override
    @Transactional
    public CustomerResponse updateMyProfile(String userEmail, CustomerProfileUpdateRequest request) {
        Customer customer = findCustomerByUserEmail(userEmail);

        if (customerRepository.existsByPhoneNumberAndIdNot(request.getPhoneNumber(), customer.getId())) {
            throw new DuplicateResourceException("Phone number already in use");
        }

        customer.setAddress(request.getAddress());
        if (request.getPhoneCountryCode() != null) {
            customer.setPhoneCountryCode(request.getPhoneCountryCode());
        }
        customer.setPhoneNumber(request.getPhoneNumber());

        User user = customer.getUser();
        if (user != null) {
            if (request.getPhoneCountryCode() != null) {
                user.setPhoneCountryCode(request.getPhoneCountryCode());
            }
            user.setPhoneNumber(request.getPhoneNumber());
            userRepository.save(user);
        }

        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id, String requesterEmail) {
        Customer customer = findCustomer(id);
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCustomer = requester.getRoles().stream()
                .anyMatch(r -> r.getName().name().equals("ROLE_CUSTOMER"));
        boolean isStaff = requester.getRoles().stream()
                .anyMatch(r -> !r.getName().name().equals("ROLE_CUSTOMER"));

        if (isCustomer && !isStaff) {
            Customer own = customerRepository.findByUserId(requester.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
            if (!own.getId().equals(id)) {
                throw new BusinessRuleException("Customers can only view their own profile");
            }
        }

        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public void activateCustomer(Long id) {
        Customer customer = findCustomer(id);
        customer.setStatus(CustomerStatus.ACTIVE);
        customerRepository.save(customer);
    }

    @Override
    @Transactional
    public void deactivateCustomer(Long id) {
        Customer customer = findCustomer(id);
        customer.setStatus(CustomerStatus.INACTIVE);
        customerRepository.save(customer);
    }

    private Customer findCustomerByUserEmail(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return customerRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer profile not found. Only self-registered customers have profiles."));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}
