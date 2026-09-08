package com.moait.moai.domain.user.dto;

import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.domain.user.entity.User;

public record UserMeResponseDTO(
        Long userId,
        String name,
        String phone,
        Gender gender,
        OnboardingStep onboardingStep
) {

    public static UserMeResponseDTO of(User user, OnboardingStep onboardingStep) {
        return new UserMeResponseDTO(
                user.getId(), user.getName(), user.getPhone(), user.getGender(), onboardingStep);
    }
}
