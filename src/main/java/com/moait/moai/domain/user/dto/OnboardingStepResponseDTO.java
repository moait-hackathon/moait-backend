package com.moait.moai.domain.user.dto;

import com.moait.moai.common.enums.OnboardingStep;

public record OnboardingStepResponseDTO(OnboardingStep onboardingStep) {

    public static OnboardingStepResponseDTO of(OnboardingStep onboardingStep) {
        return new OnboardingStepResponseDTO(onboardingStep);
    }
}
