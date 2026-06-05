package com.utilitybilling.service;

import com.utilitybilling.dto.tariff.TariffRequest;
import com.utilitybilling.dto.tariff.TariffResponse;
import com.utilitybilling.enums.MeterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TariffService {
    TariffResponse createTariff(TariffRequest request);
    TariffResponse updateTariff(Long id, TariffRequest request);
    Page<TariffResponse> getTariffs(Pageable pageable);
    Page<TariffResponse> getTariffsByType(MeterType meterType, Pageable pageable);
    void activateTariff(Long id);
    void deactivateTariff(Long id);
    TariffResponse getTariffById(Long id);
}
