package com.moait.moai.domain.user.service;

import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.repository.GoalRepository;
import com.moait.moai.domain.user.entity.User;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자의 다음 온보딩 단계를 판정한다. (규격: {@code docs/api-spec.md} "onboardingStep 값")
 *
 * <pre>
 * CONNECTED 커플 없음              → COUPLE_CONNECT
 * 커플 연결됨 + goal 없음/CANCELLED → GOAL_ONBOARDING
 * goal.status = ACTIVE / ACHIEVED  → DONE
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class OnboardingStepResolver {

    private final CoupleRepository coupleRepository;
    private final GoalRepository goalRepository;

    public OnboardingStep resolve(User user) {
        Optional<Couple> couple = coupleRepository.findConnectedByUserId(user.getId());
        if (couple.isEmpty()) {
            return OnboardingStep.COUPLE_CONNECT;
        }
        return goalRepository.findByCoupleId(couple.get().getId())
                .filter(goal -> !goal.isCancelled())
                .map(goal -> OnboardingStep.DONE)
                .orElse(OnboardingStep.GOAL_ONBOARDING);
    }
}
