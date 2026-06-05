package com.utilitybilling.service;

import com.utilitybilling.dto.reading.MeterReadingRequest;
import com.utilitybilling.dto.reading.MeterReadingResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MeterReadingService {
    MeterReadingResponse recordReading(MeterReadingRequest request);
    MeterReadingResponse updateReading(Long id, MeterReadingRequest request);
    Page<MeterReadingResponse> getReadingHistory(Long meterId, String requesterEmail, Pageable pageable);
    List<MeterReadingResponse> getMonthlyReadings(int month, int year);
}
