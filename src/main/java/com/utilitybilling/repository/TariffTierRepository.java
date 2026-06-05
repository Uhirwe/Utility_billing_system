package com.utilitybilling.repository;

import com.utilitybilling.entity.TariffTier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TariffTierRepository extends JpaRepository<TariffTier, Long> {
    List<TariffTier> findByTariffIdOrderByMinUnitsAsc(Long tariffId);
}
