package com.utilitybilling.repository;

import com.utilitybilling.entity.Bill;
import com.utilitybilling.enums.BillStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {
    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, Integer month, Integer year);
    Optional<Bill> findByBillNumber(String billNumber);
    Page<Bill> findByCustomerId(Long customerId, Pageable pageable);
    Page<Bill> findByBillingMonthAndBillingYear(Integer month, Integer year, Pageable pageable);
    Page<Bill> findByBillStatus(BillStatus status, Pageable pageable);
    List<Bill> findByBillStatusAndDueDateBeforeAndBalanceGreaterThan(
            BillStatus status, LocalDate dueDate, java.math.BigDecimal balance);
}
