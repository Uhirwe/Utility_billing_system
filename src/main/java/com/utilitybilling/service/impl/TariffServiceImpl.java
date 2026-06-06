package com.utilitybilling.service.impl;

import com.utilitybilling.dto.tariff.TariffRequest;
import com.utilitybilling.dto.tariff.TariffResponse;
import com.utilitybilling.entity.Tariff;
import com.utilitybilling.entity.TariffTier;
import com.utilitybilling.enums.AuditActionType;
import com.utilitybilling.enums.MeterType;
import com.utilitybilling.exception.BusinessRuleException;
import com.utilitybilling.exception.ResourceNotFoundException;
import com.utilitybilling.mapper.TariffMapper;
import com.utilitybilling.repository.TariffRepository;
import com.utilitybilling.service.AuditService;
import com.utilitybilling.service.TariffService;
import com.utilitybilling.util.TariffTierValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffServiceImpl implements TariffService {

    private final TariffRepository tariffRepository;
    private final TariffMapper tariffMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public TariffResponse createTariff(TariffRequest request) {
        validateTariff(request);
        int nextVersion = tariffRepository.countByMeterType(request.getMeterType()) + 1;
        Tariff tariff = tariffMapper.toEntity(request, nextVersion);
        tariff.setTiers(buildTiers(request, tariff));
        Tariff saved = tariffRepository.save(tariff);
        auditService.log("system", AuditActionType.TARIFF_UPDATED, "Tariff", saved.getId(), null, saved.getTariffName());
        return tariffMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public TariffResponse updateTariff(Long id, TariffRequest request) {
        validateTariff(request);
        Tariff tariff = findTariff(id);
        String oldName = tariff.getTariffName();
        tariffMapper.updateEntity(tariff, request);
        tariff.getTiers().clear();
        tariff.getTiers().addAll(buildTiers(request, tariff));
        Tariff saved = tariffRepository.save(tariff);
        auditService.log("system", AuditActionType.TARIFF_UPDATED, "Tariff", saved.getId(), oldName, saved.getTariffName());
        return tariffMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TariffResponse> getTariffs(Pageable pageable) {
        return tariffRepository.findAll(pageable).map(tariffMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TariffResponse> getTariffsByType(MeterType meterType, Pageable pageable) {
        return tariffRepository.findByMeterType(meterType, pageable).map(tariffMapper::toResponse);
    }

    @Override
    @Transactional
    public void activateTariff(Long id) {
        Tariff tariff = findTariff(id);
        tariff.setActive(true);
        tariffRepository.save(tariff);
    }

    @Override
    @Transactional
    public void deactivateTariff(Long id) {
        Tariff tariff = findTariff(id);
        tariff.setActive(false);
        tariffRepository.save(tariff);
    }

    @Override
    @Transactional(readOnly = true)
    public TariffResponse getTariffById(Long id) {
        return tariffMapper.toResponse(findTariff(id));
    }

    private void validateTariff(TariffRequest request) {
        if (request.getEffectiveDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Effective date must not be in the past");
        }
        TariffTierValidator.validateNonOverlapping(request.getTiers());
    }

    private List<TariffTier> buildTiers(TariffRequest request, Tariff tariff) {
        List<TariffTier> tiers = new ArrayList<>();
        if (request.getTiers() == null || request.getTiers().isEmpty()) {
            return tiers;
        }
        request.getTiers().forEach(t -> tiers.add(TariffTier.builder()
                .tariff(tariff)
                .tierName(t.getTierName())
                .minUnits(t.getMinUnits())
                .maxUnits(t.getMaxUnits())
                .ratePerUnit(t.getRatePerUnit())
                .build()));
        return tiers;
    }

    private Tariff findTariff(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found with id: " + id));
    }
}
