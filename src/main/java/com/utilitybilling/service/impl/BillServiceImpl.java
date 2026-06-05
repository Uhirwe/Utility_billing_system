package com.utilitybilling.service.impl;

import com.utilitybilling.dto.bill.BillRequest;
import com.utilitybilling.dto.bill.BillResponse;
import com.utilitybilling.dto.bill.GenerateMonthlyBillsRequest;
import com.utilitybilling.entity.Bill;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.MeterReading;
import com.utilitybilling.entity.Tariff;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.enums.BillingMode;
import com.utilitybilling.enums.BillStatus;
import com.utilitybilling.enums.CustomerStatus;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.BillMapper;
import com.utilitybilling.repository.BillRepository;
import com.utilitybilling.repository.MeterReadingRepository;
import com.utilitybilling.repository.MeterRepository;
import com.utilitybilling.repository.TariffRepository;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.service.BillService;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.EmailService;
import com.utilitybilling.util.BillNumberGenerator;
import com.utilitybilling.util.BillingCalculator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;
    private final MeterRepository meterRepository;
    private final MeterReadingRepository meterReadingRepository;
    private final TariffRepository tariffRepository;
    private final BillMapper billMapper;
    private final AuditService auditService;
    private final EmailService emailService;
    private final CustomerAccessService customerAccessService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public BillResponse generateBill(BillRequest request, String actorEmail) {
        Meter meter = meterRepository.findById(request.getMeterId())
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found"));

        validateBillable(meter);

        if (billRepository.existsByMeterIdAndBillingMonthAndBillingYear(
                request.getMeterId(), request.getBillingMonth(), request.getBillingYear())) {
            throw new DuplicateResourceException("Bill already exists for this meter and period");
        }

        MeterReading reading = meterReadingRepository
                .findByMeterIdAndReadingMonthAndReadingYear(
                        meter.getId(), request.getBillingMonth(), request.getBillingYear())
                .orElseThrow(() -> new BusinessRuleException("Meter reading must exist before bill generation"));

        if (reading.getConsumption().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Consumption must be greater than zero");
        }

        LocalDate periodDate = LocalDate.of(request.getBillingYear(), request.getBillingMonth(), 1);
        Tariff tariff = tariffRepository
                .findTopByMeterTypeAndActiveTrueAndEffectiveDateLessThanEqualOrderByVersionNumberDesc(
                        meter.getMeterType(), periodDate)
                .orElseThrow(() -> new BusinessRuleException("Active tariff must exist before bill generation"));

        BillingCalculator.ChargeBreakdown charges = tariff.getTiers().isEmpty()
                ? BillingCalculator.calculate(reading.getConsumption(), tariff.getRatePerUnit(),
                tariff.getFixedCharge(), tariff.getVatPercentage())
                : BillingCalculator.calculateWithTiers(reading.getConsumption(), tariff.getTiers(),
                tariff.getFixedCharge(), tariff.getVatPercentage());

        if (charges.totalAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Bill amount cannot be negative");
        }

        LocalDate dueDate = YearMonth.of(request.getBillingYear(), request.getBillingMonth())
                .plusMonths(1).atDay(15);

        Bill bill = Bill.builder()
                .billNumber(BillNumberGenerator.generate(meter.getId(), request.getBillingMonth(), request.getBillingYear()))
                .customer(meter.getCustomer())
                .meter(meter)
                .billingMonth(request.getBillingMonth())
                .billingYear(request.getBillingYear())
                .consumption(reading.getConsumption())
                .tariffUsed(tariff)
                .fixedCharge(tariff.getFixedCharge())
                .taxAmount(charges.taxAmount())
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(charges.totalAmount())
                .paidAmount(BigDecimal.ZERO)
                .balance(charges.totalAmount())
                .billStatus(BillStatus.UNPAID)
                .approved(false)
                .dueDate(dueDate)
                .generatedDate(LocalDateTime.now())
                .build();

        Bill saved = billRepository.save(bill);
        auditService.log(actorEmail, AuditActionType.BILL_GENERATED, "Bill", saved.getId(), null,
                saved.getBillNumber());
        emailService.sendBillGeneratedEmail(
                saved.getCustomer().getEmail(),
                saved.getCustomer().getFullNames(),
                saved.getBillNumber(),
                saved.getTotalAmount(),
                saved.getBillingMonth(),
                saved.getBillingYear());
        return billMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BillResponse approveBill(Long id, String actorEmail) {
        Bill bill = findBill(id);

        if (Boolean.TRUE.equals(bill.getApproved())) {
            throw new BusinessRuleException("Bill cannot be approved twice");
        }
        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Paid bills do not require approval");
        }

        bill.setApproved(true);
        bill.setApprovedAt(LocalDateTime.now());
        bill.setApprovedBy(actorEmail);
        bill.setBillStatus(BillStatus.APPROVED);

        Bill saved = billRepository.save(bill);
        auditService.log(actorEmail, AuditActionType.BILL_APPROVED, "Bill", saved.getId(),
                "approved=false", "approved=true by " + actorEmail);
        return billMapper.toResponse(saved);
    }

    @Override
    public Page<BillResponse> getBills(Pageable pageable) {
        return billRepository.findAll(pageable).map(billMapper::toResponse);
    }

    @Override
    public Page<BillResponse> getCustomerBills(Long customerId, Pageable pageable) {
        return billRepository.findByCustomerId(customerId, pageable).map(billMapper::toResponse);
    }

    @Override
    public Page<BillResponse> getMonthlyBills(int month, int year, Pageable pageable) {
        return billRepository.findByBillingMonthAndBillingYear(month, year, pageable)
                .map(billMapper::toResponse);
    }

    @Override
    @Transactional
    public void generateMonthlyBills(GenerateMonthlyBillsRequest request) {
        entityManager.createNativeQuery("CALL generate_monthly_bills(:month, :year)")
                .setParameter("month", request.getBillingMonth())
                .setParameter("year", request.getBillingYear())
                .executeUpdate();
    }

    @Override
    @Transactional
    public void processOverdueBills() {
        LocalDate cutoff = LocalDate.now().minusDays(30);
        List<Bill> overdueBills = billRepository.findByBillStatusAndDueDateBeforeAndBalanceGreaterThan(
                BillStatus.UNPAID, cutoff, BigDecimal.ZERO);

        for (Bill bill : overdueBills) {
            BigDecimal penalty = BillingCalculator.calculatePenalty(
                    bill.getTotalAmount(), bill.getTariffUsed().getLatePenaltyPercentage());
            bill.setPenaltyAmount(bill.getPenaltyAmount().add(penalty));
            bill.setTotalAmount(bill.getTotalAmount().add(penalty));
            bill.setBalance(bill.getBalance().add(penalty));
            billRepository.save(bill);

            Meter meter = bill.getMeter();
            if (meter.getStatus() == MeterStatus.ACTIVE) {
                meter.setStatus(MeterStatus.DISCONNECTED);
                meterRepository.save(meter);
                auditService.log("system", AuditActionType.METER_DISCONNECTED, "Meter", meter.getId(),
                        MeterStatus.ACTIVE.name(), MeterStatus.DISCONNECTED.name());
            }
        }
    }

    @Override
    public BillResponse getBillById(Long id, String requesterEmail) {
        Bill bill = findBill(id);
        customerAccessService.assertStaffOrOwnCustomer(requesterEmail, bill.getCustomer().getId());
        return billMapper.toResponse(bill);
    }

    private Bill findBill(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
    }

    private void validateBillable(Meter meter) {
        if (meter.getBillingMode() == BillingMode.PREPAID) {
            throw new BusinessRuleException("Prepaid meters are not billed monthly — use postpaid mode for WASAC/REG billing");
        }
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot bill inactive or disconnected meter");
        }
        if (meter.getCustomer().getStatus() != CustomerStatus.ACTIVE) {
            throw new BusinessRuleException("Cannot bill inactive or suspended customer");
        }
    }
}
