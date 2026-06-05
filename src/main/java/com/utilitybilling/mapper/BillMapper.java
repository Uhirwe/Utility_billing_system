package com.utilitybilling.mapper;

import com.utilitybilling.dto.bill.BillResponse;
import com.utilitybilling.entity.Bill;
import org.springframework.stereotype.Component;

@Component
public class BillMapper {

    public BillResponse toResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billNumber(bill.getBillNumber())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullNames())
                .meterId(bill.getMeter().getId())
                .meterNumber(bill.getMeter().getMeterNumber())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .consumption(bill.getConsumption())
                .tariffId(bill.getTariffUsed().getId())
                .tariffName(bill.getTariffUsed().getTariffName())
                .fixedCharge(bill.getFixedCharge())
                .taxAmount(bill.getTaxAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .balance(bill.getBalance())
                .billStatus(bill.getBillStatus())
                .approved(bill.getApproved())
                .approvedAt(bill.getApprovedAt())
                .approvedBy(bill.getApprovedBy())
                .dueDate(bill.getDueDate())
                .generatedDate(bill.getGeneratedDate())
                .createdAt(bill.getCreatedAt())
                .build();
    }
}
