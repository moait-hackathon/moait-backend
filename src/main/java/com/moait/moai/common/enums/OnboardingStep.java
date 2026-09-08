package com.moait.moai.common.enums;

/**
 * 온보딩 진행 단계 (회원가입/로그인 응답의 다음 진행 지점).
 *
 * <p>온보딩 = <b>커플 연결 → 공동 목표 5단계 입력</b> (2026-09 개편). 개인별 투자성향 설문은 폐기.
 * 판정 로직은 {@code OnboardingStepResolver}, 규격은 {@code docs/api-spec.md} 참고.
 *
 * <pre>
 * COUPLE_CONNECT   CONNECTED 커플 없음
 * GOAL_ONBOARDING  커플 연결됨 + goal 없거나 status = DRAFT
 * DONE             goal.status = ACTIVE 이상
 * </pre>
 */
public enum OnboardingStep {
    COUPLE_CONNECT,
    GOAL_ONBOARDING,
    DONE
}
