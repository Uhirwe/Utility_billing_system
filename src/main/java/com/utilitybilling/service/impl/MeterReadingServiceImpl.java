package com.utilitybilling.service.impl;

import com.utilitybilling.dto.reading.MeterReadingRequest;
import com.utilitybilling.dto.reading.MeterReadingResponse;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.entity.MeterReading;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.MeterReadingMapper;
import com.utilitybilling.repository.MeterReadingRepository;
import com.utilitybilling.repository.MeterRepository;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.service.CustomerAccessService;
import com.utilitybilling.service.MeterReadingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingServiceImpl implements MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterRepository meterRepository;
    private final MeterReadingMapper meterReadingMapper;
    private final AuditService auditService;
    private final CustomerAccessService customerAccessService;

    @Override
    @Transactional
    public MeterReadingResponse recordReading(MeterReadingRequest request) {
        Meter meter = findMeter(request.getMeterId());
        validateMeterActive(meter);
        validateReadings(request.getPreviousReading(), request.getCurrentReading());
        validateReadingContinuity(meter.getId(), request.getPreviousReading(), null);

        if (request.getReadingDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Reading date must not be in the future");
        }

        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();

        if (meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYear(meter.getId(), month, year)) {
            throw new DuplicateResourceException("Only one reading per meter per month/year is allowed");
        }

        MeterReading reading = buildReading(meter, request, month, year);
        MeterReading saved = meterReadingRepository.save(reading);
        auditService.log("system", AuditActionType.METER_READING_RECORDED, "MeterReading", saved.getId(),
                null, "consumption=" + saved.getConsumption());
        return meterReadingMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public MeterReadingResponse updateReading(Long id, MeterReadingRequest request) {
        MeterReading reading = findReading(id);
        Meter meter = findMeter(request.getMeterId());
        validateMeterActive(meter);
        validateReadings(request.getPreviousReading(), request.getCurrentReading());
        validateReadingContinuity(meter.getId(), request.getPreviousReading(), id);

        if (request.getReadingDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Reading date must not be in the future");
        }

        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();

        if (meterReadingRepository.existsByMeterIdAndReadingMonthAndReadingYearAndIdNot(
                meter.getId(), month, year, id)) {
            throw new DuplicateResourceException("Only one reading per meter per month/year is allowed");
        }

        reading.setMeter(meter);
        reading.setPreviousReading(request.getPreviousReading());
        reading.setCurrentReading(request.getCurrentReading());
        reading.setReadingDate(request.getReadingDate());
        reading.setReadingMonth(month);
        reading.setReadingYear(year);
        reading.setConsumption(request.getCurrentReading().subtract(request.getPreviousReading()));

        return meterReadingMapper.toResponse(meterReadingRepository.save(reading));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MeterReadingResponse> getReadingHistory(Long meterId, String requesterEmail, Pageable pageable) {
        Meter meter = findMeter(meterId);
        customerAccessService.assertStaffOrOwnCustomer(requesterEmail, meter.getCustomer().getId());
        return meterReadingRepository.findByMeterId(meterId, pageable)
                .map(meterReadingMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterReadingResponse> getMonthlyReadings(int month, int year) {
        return meterReadingRepository.findByReadingMonthAndReadingYear(month, year).stream()
                .map(meterReadingMapper::toResponse)
                .toList();
    }

    private MeterReading buildReading(Meter meter, MeterReadingRequest request, int month, int year) {
        BigDecimal consumption = request.getCurrentReading().subtract(request.getPreviousReading());
        return MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .consumption(consumption)
                .readingDate(request.getReadingDate())
                .readingMonth(month)
                .readingYear(year)
                .build();
    }

    private void validateMeterActive(Meter meter) {
        if (meter.getStatus() != MeterStatus.ACTIVE) {
            throw new BusinessRuleException("Meter must be ACTIVE to record readings");
        }
    }

    private void validateReadings(BigDecimal previous, BigDecimal current) {
        if (current.compareTo(previous) <= 0) {
            throw new BusinessRuleException("Current reading must be greater than previous reading");
        }
    }

    private void validateReadingContinuity(Long meterId, BigDecimal previousReading, Long excludeReadingId) {
        meterReadingRepository.findTopByMeterIdOrderByReadingYearDescReadingMonthDesc(meterId)
                .filter(last -> excludeReadingId == null || !last.getId().equals(excludeReadingId))
                .ifPresent(last -> {
                    if (previousReading.compareTo(last.getCurrentReading()) != 0) {
                        throw new BusinessRuleException(
                                "Previous reading must match last recorded current reading: " + last.getCurrentReading());
                    }
                });
    }

    private Meter findMeter(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
    }

    private MeterReading findReading(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reading not found with id: " + id));
    }
}
