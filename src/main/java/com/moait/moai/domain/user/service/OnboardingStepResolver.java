package com.moait.moai.domain.user.service;

import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.domain.user.entity.User;
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
 * <p><b>TODO</b> — 커플/공동목표 도메인 구현 전까지는 항상 {@link OnboardingStep#COUPLE_CONNECT}
 * 를 반환한다. 커플 슬라이스에서 {@code CoupleRepository}, 목표 슬라이스에서 {@code GoalRepository}
 * 를 주입해 위 규칙대로 완성할 것. (규격: {@code docs/api-spec.md} "onboardingStep 값")
 */
@Component
public class OnboardingStepResolver {

    public OnboardingStep resolve(User user) {
        // TODO(couple/goal): 커플 연결 여부 + goal.status 로 GOAL_ONBOARDING / DONE 판정
        return OnboardingStep.COUPLE_CONNECT;
    }
}
