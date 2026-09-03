package com.moait.moai.domain.auth.dto;

import com.moait.moai.common.enums.OnboardingStep;

/**
 * 인증 성공 응답.
 *
 * <p>현재는 액세스 토큰만 발급한다. 리프레시 토큰 기반 세션 유지(재발급)는 Redis 도입과 함께 추후 추가.
 */
public record AuthTokenResponseDTO(
        Long userId,
        String accessToken,
        OnboardingStep onboardingStep
) {

    public static AuthTokenResponseDTO of(Long userId, String accessToken, OnboardingStep onboardingStep) {
        return new AuthTokenResponseDTO(userId, accessToken, onboardingStep);
    }
}
