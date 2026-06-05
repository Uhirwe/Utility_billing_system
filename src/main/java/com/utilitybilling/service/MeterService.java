package com.utilitybilling.service;

import com.utilitybilling.dto.meter.MeterRequest;
import com.utilitybilling.dto.meter.MeterResponse;

import java.util.List;

public interface MeterService {
    MeterResponse createMeter(MeterRequest request);
    MeterResponse assignMeter(Long meterId, Long customerId);
    MeterResponse updateMeter(Long id, MeterRequest request);
    void activateMeter(Long id);
    void deactivateMeter(Long id);
    List<MeterResponse> getCustomerMeters(Long customerId);
    MeterResponse getMeterById(Long id);
}
