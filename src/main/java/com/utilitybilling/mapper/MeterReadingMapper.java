package com.utilitybilling.mapper;

import com.utilitybilling.dto.reading.MeterReadingResponse;
import com.utilitybilling.entity.MeterReading;
import org.springframework.stereotype.Component;

@Component
public class MeterReadingMapper {

    public MeterReadingResponse toResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .consumption(reading.getConsumption())
                .readingDate(reading.getReadingDate())
                .readingMonth(reading.getReadingMonth())
                .readingYear(reading.getReadingYear())
                .createdAt(reading.getCreatedAt())
                .build();
    }
}
