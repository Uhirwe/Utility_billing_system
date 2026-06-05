package com.utilitybilling.mapper;

import com.utilitybilling.dto.tariff.TariffRequest;
import com.utilitybilling.dto.tariff.TariffResponse;
import com.utilitybilling.entity.Tariff;
import org.springframework.stereotype.Component;

@Component
public class TariffMapper {

    public Tariff toEntity(TariffRequest request, int versionNumber) {
        return Tariff.builder()
                .meterType(request.getMeterType())
                .tariffName(request.getTariffName())
                .ratePerUnit(request.getRatePerUnit())
                .fixedCharge(request.getFixedCharge())
                .vatPercentage(request.getVatPercentage())
                .latePenaltyPercentage(request.getLatePenaltyPercentage())
                .versionNumber(versionNumber)
                .effectiveDate(request.getEffectiveDate())
                .active(true)
                .build();
    }

    public void updateEntity(Tariff tariff, TariffRequest request) {
        tariff.setTariffName(request.getTariffName());
        tariff.setRatePerUnit(request.getRatePerUnit());
        tariff.setFixedCharge(request.getFixedCharge());
        tariff.setVatPercentage(request.getVatPercentage());
        tariff.setLatePenaltyPercentage(request.getLatePenaltyPercentage());
        tariff.setEffectiveDate(request.getEffectiveDate());
    }

    public TariffResponse toResponse(Tariff tariff) {
        return TariffResponse.builder()
                .id(tariff.getId())
                .meterType(tariff.getMeterType())
                .tariffName(tariff.getTariffName())
                .ratePerUnit(tariff.getRatePerUnit())
                .fixedCharge(tariff.getFixedCharge())
                .vatPercentage(tariff.getVatPercentage())
                .latePenaltyPercentage(tariff.getLatePenaltyPercentage())
                .versionNumber(tariff.getVersionNumber())
                .effectiveDate(tariff.getEffectiveDate())
                .active(tariff.getActive())
                .createdAt(tariff.getCreatedAt())
                .build();
    }
}
