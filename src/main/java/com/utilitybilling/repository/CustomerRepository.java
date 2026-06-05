package com.utilitybilling.repository;

import com.utilitybilling.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByNationalId(String nationalId);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    boolean existsByNationalIdAndIdNot(String nationalId, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);
    boolean existsByPhoneNumberAndIdNot(String phoneNumber, Long id);
    Optional<Customer> findByUserId(Long userId);
    Optional<Customer> findByEmail(String email);
}
