package com.moait.moai.domain.couple.dto;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.user.entity.User;
import java.time.LocalDateTime;

/**
 * {@code GET /couples/me} — 연결된 커플 + 파트너 정보. 공동 목표/온보딩 진입 확인용.
 *
 * <p>{@code jointRiskProfileType} 은 공동 목표 온보딩 완료 후에만 존재 (그 전엔 {@code null}).
 */
public record CoupleMeResponseDTO(
        Long coupleId,
        CoupleStatus status,
        LocalDateTime connectedAt,
        CoupleMemberDTO me,
        CoupleMemberDTO partner,
        OnboardingStep onboardingStep,
        RiskProfileType jointRiskProfileType
) {

    public static CoupleMeResponseDTO of(Couple couple, User me, User partner,
                                         OnboardingStep onboardingStep,
                                         RiskProfileType jointRiskProfileType) {
        return new CoupleMeResponseDTO(
                couple.getId(),
                couple.getStatus(),
                couple.getConnectedAt(),
                CoupleMemberDTO.of(me),
                CoupleMemberDTO.of(partner),
                onboardingStep,
                jointRiskProfileType);
    }
}
