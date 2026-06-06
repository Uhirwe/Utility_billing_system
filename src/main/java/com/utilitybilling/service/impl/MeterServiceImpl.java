package com.utilitybilling.service.impl;

import com.utilitybilling.dto.meter.MeterRequest;
import com.utilitybilling.dto.meter.MeterResponse;
import com.utilitybilling.entity.Customer;
import com.utilitybilling.entity.Meter;
import com.utilitybilling.enums.BillingMode;
import com.utilitybilling.enums.MeterStatus;
import com.utilitybilling.enums.MeterType;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.DuplicateResourceException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.MeterMapper;
import com.utilitybilling.repository.CustomerRepository;
import com.utilitybilling.repository.MeterRepository;
import com.utilitybilling.service.MeterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterServiceImpl implements MeterService {

    private final MeterRepository meterRepository;
    private final CustomerRepository customerRepository;
    private final MeterMapper meterMapper;

    @Override
    @Transactional
    public MeterResponse createMeter(MeterRequest request) {
        validateMeterNumberFormat(request.getMeterNumber(), request.getMeterType());

        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists: " + request.getMeterNumber());
        }

        Customer customer = findCustomer(request.getCustomerId());

        if (request.getInstallationDate().isAfter(java.time.LocalDate.now())) {
            throw new BusinessRuleException("Installation date cannot be in the future");
        }

        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(MeterStatus.ACTIVE)
                .billingMode(request.getBillingMode() != null ? request.getBillingMode() : BillingMode.POSTPAID)
                .customer(customer)
                .build();

        return meterMapper.toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterResponse assignMeter(Long meterId, Long customerId) {
        Meter meter = findMeter(meterId);
        Customer customer = findCustomer(customerId);
        meter.setCustomer(customer);
        return meterMapper.toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public MeterResponse updateMeter(Long id, MeterRequest request) {
        Meter meter = findMeter(id);
        validateMeterNumberFormat(request.getMeterNumber(), request.getMeterType());

        if (!meter.getMeterNumber().equals(request.getMeterNumber())
                && meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists");
        }

        meter.setMeterNumber(request.getMeterNumber());
        meter.setMeterType(request.getMeterType());
        meter.setInstallationDate(request.getInstallationDate());

        if (!meter.getCustomer().getId().equals(request.getCustomerId())) {
            assignMeter(id, request.getCustomerId());
            meter = findMeter(id);
            meter.setMeterNumber(request.getMeterNumber());
            meter.setMeterType(request.getMeterType());
            meter.setInstallationDate(request.getInstallationDate());
        }

        return meterMapper.toResponse(meterRepository.save(meter));
    }

    @Override
    @Transactional
    public void activateMeter(Long id) {
        Meter meter = findMeter(id);
        meter.setStatus(MeterStatus.ACTIVE);
        meterRepository.save(meter);
    }

    @Override
    @Transactional
    public void deactivateMeter(Long id) {
        Meter meter = findMeter(id);
        meter.setStatus(MeterStatus.INACTIVE);
        meterRepository.save(meter);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MeterResponse> getCustomerMeters(Long customerId) {
        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with id: " + customerId);
        }
        return meterRepository.findByCustomerId(customerId).stream()
                .map(meterMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MeterResponse getMeterById(Long id) {
        return meterMapper.toResponse(findMeter(id));
    }

    private void validateMeterNumberFormat(String meterNumber, MeterType meterType) {
        String prefix = meterType == MeterType.WATER ? "WM" : "EM";
        if (!meterNumber.startsWith(prefix + "-")) {
            throw new BusinessRuleException("Meter number must start with " + prefix + "- for " + meterType);
        }
    }

    private Meter findMeter(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found with id: " + id));
    }

    private Customer findCustomer(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}
