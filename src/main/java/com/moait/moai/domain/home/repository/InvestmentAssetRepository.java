package com.moait.moai.domain.home.repository;

import com.moait.moai.domain.home.entity.InvestmentAsset;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAssetRepository extends JpaRepository<InvestmentAsset, Long> {

    List<InvestmentAsset> findAllByInvestmentAccountIdInAndActiveTrue(Collection<Long> accountIds);
}
