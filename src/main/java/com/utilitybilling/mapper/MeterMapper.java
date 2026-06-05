package com.utilitybilling.mapper;

import com.utilitybilling.dto.meter.MeterResponse;
import com.utilitybilling.entity.Meter;
import org.springframework.stereotype.Component;

@Component
public class MeterMapper {

    public MeterResponse toResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .billingMode(meter.getBillingMode())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullNames())
                .createdAt(meter.getCreatedAt())
                .updatedAt(meter.getUpdatedAt())
                .build();
    }
}
