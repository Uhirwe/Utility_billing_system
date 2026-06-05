package com.utilitybilling.service;

import com.utilitybilling.dto.bill.BillRequest;
import com.utilitybilling.dto.bill.BillResponse;
import com.utilitybilling.dto.bill.GenerateMonthlyBillsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BillService {
    BillResponse generateBill(BillRequest request, String actorEmail);
    BillResponse approveBill(Long id, String actorEmail);
    Page<BillResponse> getBills(Pageable pageable);
    Page<BillResponse> getCustomerBills(Long customerId, Pageable pageable);
    Page<BillResponse> getMonthlyBills(int month, int year, Pageable pageable);
    void generateMonthlyBills(GenerateMonthlyBillsRequest request);
    void processOverdueBills();
    BillResponse getBillById(Long id, String requesterEmail);
}
