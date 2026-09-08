package com.moait.moai.domain.home.repository;

import com.moait.moai.domain.home.entity.InvestmentAssetSnapshot;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvestmentAssetSnapshotRepository extends JpaRepository<InvestmentAssetSnapshot, Long> {

    List<InvestmentAssetSnapshot> findAllByInvestmentAssetIdInOrderBySnapshotDateAsc(
            Collection<Long> assetIds);
}
