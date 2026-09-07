package com.moait.moai.domain.user.dto;

import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.user.entity.InvestmentProfile;

public record InvestmentProfileResponseDTO(
        Long investmentProfileId,
        RiskProfileType riskProfileType,
        String riskProfileTypeLabel,
        Integer riskProfileScore,
        String summary,
        OnboardingStep onboardingStep
) {

    public static InvestmentProfileResponseDTO of(InvestmentProfile profile, OnboardingStep onboardingStep) {
        RiskProfileType type = profile.getRiskProfileType();
        return new InvestmentProfileResponseDTO(
                profile.getId(),
                type,
                type.getLabel(),
                profile.getRiskProfileScore(),
                type.getSummary(),
                onboardingStep);
    }
}
