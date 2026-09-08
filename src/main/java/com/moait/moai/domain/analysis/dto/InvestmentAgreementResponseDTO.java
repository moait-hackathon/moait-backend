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
