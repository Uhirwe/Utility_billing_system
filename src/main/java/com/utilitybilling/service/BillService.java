package com.utilitybilling.service;

import com.utilitybilling.dto.bill.ApproveBillResponse;
import com.utilitybilling.dto.bill.BillRequest;
import com.utilitybilling.dto.bill.BillResponse;
import com.utilitybilling.dto.bill.GenerateMonthlyBillsRequest;
import com.utilitybilling.dto.bill.GenerateMonthlyBillsResponse;
import com.utilitybilling.dto.email.EmailDeliveryResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillService {
    BillResponse generateBill(BillRequest request, String actorEmail);
    ApproveBillResponse approveBill(Long id, String actorEmail);
    EmailDeliveryResult resendBillEmail(Long id);
    Page<BillResponse> getBills(Pageable pageable);
    Page<BillResponse> getCustomerBills(Long customerId, Pageable pageable);
    Page<BillResponse> getMonthlyBills(int month, int year, Pageable pageable);
    GenerateMonthlyBillsResponse generateMonthlyBills(GenerateMonthlyBillsRequest request);
    void processOverdueBills();
    BillResponse getBillById(Long id, String requesterEmail);
}
