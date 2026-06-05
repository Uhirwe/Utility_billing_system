package com.utilitybilling.repository;

import com.utilitybilling.entity.MeterReading;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {
    boolean existsByMeterIdAndReadingMonthAndReadingYear(Long meterId, Integer month, Integer year);
    boolean existsByMeterIdAndReadingMonthAndReadingYearAndIdNot(Long meterId, Integer month, Integer year, Long id);
    Optional<MeterReading> findByMeterIdAndReadingMonthAndReadingYear(Long meterId, Integer month, Integer year);
    Page<MeterReading> findByMeterId(Long meterId, Pageable pageable);
    List<MeterReading> findByReadingMonthAndReadingYear(Integer month, Integer year);
    Optional<MeterReading> findTopByMeterIdOrderByReadingYearDescReadingMonthDesc(Long meterId);
}
