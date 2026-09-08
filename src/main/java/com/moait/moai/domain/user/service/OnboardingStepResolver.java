package com.moait.moai.domain.user.service;

import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자의 다음 온보딩 단계를 판정한다.
 *
 * <pre>
 * CONNECTED 커플 없음                     → COUPLE_CONNECT
 * 커플 연결됨 + goal 없거나 status=DRAFT   → GOAL_ONBOARDING
 * goal.status = ACTIVE 이상               → DONE
 * </pre>
 *
 * <p><b>TODO</b>(goal) — 공동목표 도메인 구현 전까지 커플 연결 시 항상 {@link OnboardingStep#GOAL_ONBOARDING}
 * 를 반환한다. goal 슬라이스에서 {@code GoalRepository} 를 주입해 {@code goal.status = ACTIVE} 이상이면
 * {@link OnboardingStep#DONE} 을 반환하도록 완성할 것. (규격: {@code docs/api-spec.md} "onboardingStep 값")
 */
@Component
@RequiredArgsConstructor
public class OnboardingStepResolver {

    private final CoupleRepository coupleRepository;

    public OnboardingStep resolve(User user) {
        if (coupleRepository.findConnectedByUserId(user.getId()).isEmpty()) {
            return OnboardingStep.COUPLE_CONNECT;
        }
        // TODO(goal): goal.status = ACTIVE 이상이면 DONE
        return OnboardingStep.GOAL_ONBOARDING;
    }
}
