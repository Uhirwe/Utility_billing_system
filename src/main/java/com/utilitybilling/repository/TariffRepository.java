package com.utilitybilling.repository;

import com.utilitybilling.entity.Tariff;
import com.utilitybilling.enums.MeterType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {
    Optional<Tariff> findTopByMeterTypeAndActiveTrueAndEffectiveDateLessThanEqualOrderByVersionNumberDesc(
            MeterType meterType, LocalDate effectiveDate);
    org.springframework.data.domain.Page<Tariff> findByMeterType(MeterType meterType, org.springframework.data.domain.Pageable pageable);
    int countByMeterType(MeterType meterType);
}
