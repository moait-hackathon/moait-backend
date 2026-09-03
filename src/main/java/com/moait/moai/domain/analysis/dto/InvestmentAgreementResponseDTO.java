package com.moait.moai.domain.analysis.dto;

import java.util.List;

public record InvestmentAgreementResponseDTO(
        RiskScore personA,
        RiskScore personB,
        RiskScore jointFund,
        Recommendation recommendation,
        GoalRequirement goalRequirement,
        Agreement agreement) {

    public record RiskScore(Integer preferenceScore, Integer userLimit,
                            Integer serviceLimit, Integer finalLimit, String profileType) { }

    public record Recommendation(Integer weightedScore, Integer centerScore,
                                 Integer rangeMin, Integer rangeMax, Integer finalMax) { }

    public record GoalRequirement(Double requiredAnnualReturnRate, Integer rangeMin,
                                  Integer rangeMax, Boolean realistic) { }

    public record Agreement(String status, Integer recommendedRiskScore, String summary,
                            String rationale, List<String> alternatives,
                            List<String> cautions, Boolean aiGenerated) { }
}
