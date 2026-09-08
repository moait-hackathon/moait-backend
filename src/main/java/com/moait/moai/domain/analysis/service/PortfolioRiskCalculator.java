package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.AssetPosition;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.RiskScore;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** 보유 비중 기반 추정 성향이며 개인의 손실 허용 상한을 추정하지 않는다. */
@Component
public class PortfolioRiskCalculator {
    private static final Map<String, Integer> RISK_SCORES = Map.of(
            "VERY_LOW", 0, "LOW", 25, "MEDIUM", 50, "HIGH", 75, "VERY_HIGH", 100);
    private static final Map<String, Integer> TYPE_DEFAULTS = Map.of(
            "CASH", 0, "DEPOSIT", 0, "SAVINGS", 0, "BOND", 25, "STOCK", 75, "CRYPTO", 100);

    public RiskScore calculate(List<AssetPosition> positions, String person) {
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal weighted = BigDecimal.ZERO;
        for (var position : positions) {
            BigDecimal value = position.currentValue();
            if (value == null || value.signum() < 0) {
                throw incomplete(person, "자산 current_value는 0 이상의 평가금액이어야 합니다.");
            }
            if (value.signum() == 0) continue;
            if (!"KRW".equals(position.currencyCode())) {
                throw incomplete(person, "외화 자산의 원화 환산 평가금액이 필요합니다. 현재 KRW 자산만 계산할 수 있습니다.");
            }
            Integer score = position.riskLevel() == null ? null : RISK_SCORES.get(position.riskLevel());
            if (score == null && "UNCLASSIFIED".equals(position.riskLevel()) && position.assetType() != null) {
                score = TYPE_DEFAULTS.get(position.assetType());
            }
            if (score == null) {
                throw incomplete(person, "자산 risk_level 분류를 완료해 주세요. ETF·펀드·연금·기타 자산은 위험등급이 필요합니다.");
            }
            total = total.add(value);
            weighted = weighted.add(value.multiply(BigDecimal.valueOf(score)));
        }
        if (total.signum() == 0) {
            throw incomplete(person, "활성 계좌에 평가금액이 양수인 자산을 등록하거나 동기화해 주세요.");
        }
        int score = weighted.divide(total, 0, RoundingMode.HALF_UP).intValueExact();
        return new RiskScore(score, null, null, null, InvestmentAnalysisServiceImpl.profileType(score));
    }

    private InvestmentAnalysisDataException incomplete(String person, String reason) {
        return new InvestmentAnalysisDataException("개인 " + person + ": " + reason);
    }
}
