package com.moait.moai.domain.analysis.dto;

import java.util.List;
import java.math.BigDecimal;

public record InvestmentAgreementResponseDTO(
        RiskScore personA,
        RiskScore personB,
        RiskScore jointFund,
        Recommendation recommendation,
        GoalRequirement goalRequirement,
        Agreement agreement) {

    /** A·B는 보유 자산 기반 점수이며 추정하지 않는 세 상한 필드는 null이다. C는 공동 설문 상한을 포함한다. */
    public record RiskScore(Integer preferenceScore, Integer userLimit,
                            Integer serviceLimit, Integer finalLimit, String profileType) { }

    public record Recommendation(BigDecimal weightedScore, BigDecimal centerScore,
                                 Integer rangeMin, Integer rangeMax, Integer finalMax) { }

    public record GoalRequirement(Double requiredAnnualReturnRate, Integer rangeMin,
                                  Integer rangeMax, Boolean realistic, Integer investmentMonths, String calculationMethod) { }

    public record Agreement(String status, Integer recommendedRiskScore, String summary,
                            String rationale, String recommendedStrategy, List<String> alternatives,
                            List<String> cautions, Boolean aiGenerated) { }
}
