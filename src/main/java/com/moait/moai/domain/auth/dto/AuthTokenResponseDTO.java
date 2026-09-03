package com.moait.moai.domain.auth.dto;

import com.moait.moai.common.enums.OnboardingStep;

public record AuthTokenResponseDTO(
        Long userId,
        String accessToken,
        String refreshToken,
        OnboardingStep onboardingStep
) {

    public static AuthTokenResponseDTO of(Long userId, String accessToken, String refreshToken,
                                          OnboardingStep onboardingStep) {
        return new AuthTokenResponseDTO(userId, accessToken, refreshToken, onboardingStep);
    }
}
