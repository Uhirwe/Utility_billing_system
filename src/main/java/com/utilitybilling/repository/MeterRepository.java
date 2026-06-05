package com.utilitybilling.repository;

import com.utilitybilling.entity.Meter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MeterRepository extends JpaRepository<Meter, Long> {
    boolean existsByMeterNumber(String meterNumber);
    List<Meter> findByCustomerId(Long customerId);
}
