package com.moait.moai.domain.home.repository;

import com.moait.moai.domain.home.entity.HomeInvestmentAsset;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomeInvestmentAssetRepository extends JpaRepository<HomeInvestmentAsset, Long> {

    List<HomeInvestmentAsset> findAllByInvestmentAccountIdInAndActiveTrue(Collection<Long> accountIds);
}
