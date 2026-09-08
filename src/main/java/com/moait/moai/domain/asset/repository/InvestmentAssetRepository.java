package com.moait.moai.domain.asset.repository;

import com.moait.moai.domain.asset.entity.InvestmentAsset;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface InvestmentAssetRepository extends JpaRepository<InvestmentAsset, Long> {
    @Transactional(readOnly = true)
    @Query(value = """
            SELECT CASE WHEN a.user_id = c.male_id THEN 'A' ELSE 'B' END AS person,
                   s.asset_type AS assetType, s.risk_level AS riskLevel,
                   s.current_value AS currentValue, s.currency_code AS currencyCode
            FROM couple c
            JOIN investment_account a ON (a.user_id = c.male_id OR a.user_id = c.female_id)
            JOIN investment_asset s ON s.investment_account_id = a.investment_account_id
            WHERE c.id = :coupleId AND c.status = 'CONNECTED'
              AND a.is_active = TRUE AND s.is_active = TRUE
            """, nativeQuery = true)
    List<PortfolioPosition> findActivePositionsByCoupleId(@Param("coupleId") Long coupleId);

    interface PortfolioPosition {
        String getPerson();
        String getAssetType();
        String getRiskLevel();
        BigDecimal getCurrentValue();
        String getCurrencyCode();
    }
}
