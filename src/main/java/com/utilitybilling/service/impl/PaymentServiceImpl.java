package com.utilitybilling.service.impl;

import com.utilitybilling.dto.payment.PaymentRequest;
import com.utilitybilling.dto.payment.PaymentResponse;
import com.utilitybilling.entity.Bill;
import com.utilitybilling.entity.Payment;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.enums.BillStatus;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.PaymentMapper;
import com.utilitybilling.repository.BillRepository;
import com.utilitybilling.repository.MeterRepository;
import com.utilitybilling.repository.PaymentRepository;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.service.EmailService;
import com.utilitybilling.service.PaymentService;
import com.utilitybilling.util.BillNumberGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final MeterRepository meterRepository;
    private final PaymentMapper paymentMapper;
    private final AuditService auditService;
    private final EmailService emailService;

    @Override
    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billRepository.findById(request.getBillId())
                .orElseThrow(() -> new ResourceNotFoundException("Bill must exist before payment"));

        if (!Boolean.TRUE.equals(bill.getApproved())) {
            throw new BusinessRuleException("Bill must be approved by Finance before payment can be processed");
        }

        if (bill.getBillStatus() == BillStatus.PAID) {
            throw new BusinessRuleException("Bill is already fully paid");
        }

        if (request.getAmountPaid().compareTo(bill.getBalance()) > 0) {
            throw new BusinessRuleException("Overpayment is not allowed. Remaining balance: " + bill.getBalance());
        }

        LocalDateTime paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDateTime.now();
        if (paymentDate.isAfter(LocalDateTime.now())) {
            throw new BusinessRuleException("Payment date cannot be in the future");
        }

        Payment payment = Payment.builder()
                .paymentReference(BillNumberGenerator.generatePaymentReference())
                .bill(bill)
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(paymentDate)
                .build();

        paymentRepository.save(payment);

        BigDecimal newPaidAmount = bill.getPaidAmount().add(request.getAmountPaid());
        BigDecimal newBalance = bill.getTotalAmount().subtract(newPaidAmount);

        bill.setPaidAmount(newPaidAmount);
        bill.setBalance(newBalance.max(BigDecimal.ZERO));

        if (newBalance.compareTo(BigDecimal.ZERO) <= 0) {
            bill.setBillStatus(BillStatus.PAID);
            bill.setBalance(BigDecimal.ZERO);
            reconnectMeterIfNeeded(bill);
        } else {
            bill.setBillStatus(BillStatus.PARTIALLY_PAID);
        }

        billRepository.save(bill);
        auditService.log("system", AuditActionType.PAYMENT_PROCESSED, "Payment", payment.getId(), null,
                payment.getPaymentReference() + " amount=" + payment.getAmountPaid());

        emailService.sendPaymentConfirmationEmail(
                bill.getCustomer().getEmail(),
                bill.getCustomer().getFullNames(),
                payment.getPaymentReference(),
                payment.getAmountPaid(),
                bill.getBillNumber());

        return paymentMapper.toResponse(payment);
    }

    private void reconnectMeterIfNeeded(Bill bill) {
        var meter = bill.getMeter();
        if (meter.getStatus() == MeterStatus.DISCONNECTED) {
            boolean hasUnpaid = billRepository.findByCustomerId(bill.getCustomer().getId(), Pageable.unpaged())
                    .stream()
                    .anyMatch(b -> b.getBalance().compareTo(BigDecimal.ZERO) > 0);
            if (!hasUnpaid) {
                meter.setStatus(MeterStatus.ACTIVE);
                meterRepository.save(meter);
                auditService.log("system", AuditActionType.METER_RECONNECTED, "Meter", meter.getId(),
                        MeterStatus.DISCONNECTED.name(), MeterStatus.ACTIVE.name());
            }
        }
    }

    @Override
    public Page<PaymentResponse> getPaymentHistory(Long billId, Pageable pageable) {
        return paymentRepository.findByBillId(billId, pageable).map(paymentMapper::toResponse);
    }

    @Override
    public Page<PaymentResponse> getCustomerPaymentHistory(Long customerId, Pageable pageable) {
        return paymentRepository.findByBillCustomerId(customerId, pageable).map(paymentMapper::toResponse);
    }
}
