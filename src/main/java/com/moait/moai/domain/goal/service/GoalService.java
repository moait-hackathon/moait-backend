package com.moait.moai.domain.goal.service;

import com.moait.moai.domain.goal.dto.CurrentAmountResponseDTO;
import com.moait.moai.domain.goal.dto.GoalCancelResponseDTO;
import com.moait.moai.domain.goal.dto.GoalOnboardingRequestDTO;
import com.moait.moai.domain.goal.dto.GoalResponseDTO;
import com.moait.moai.domain.goal.dto.GoalUpdateRequestDTO;
import com.moait.moai.domain.goal.dto.RiskProfileRecalculateResponseDTO;

public interface GoalService {

    /** 온보딩 5단계 제출 → 커플당 1개 목표 생성 (R 산출, status=ACTIVE). */
    GoalResponseDTO createGoal(Long userId, GoalOnboardingRequestDTO request);

    /** 공동 목표 + 진척률 + R 조회. */
    GoalResponseDTO getMyGoal(Long userId);

    /** 마이페이지 - 온보딩 답변 부분 수정. 점수 문항/targetDate 변경 시 R 재계산. */
    GoalResponseDTO updateGoal(Long userId, GoalUpdateRequestDTO request);

    /** 진척 금액 갱신. 목표 도달 시 ACHIEVED 자동 전환. */
    CurrentAmountResponseDTO updateCurrentAmount(Long userId, long currentAmount);

    /** 저장된 답변으로 investmentPeriodMonths·R·jointRiskProfileType 강제 재계산 (멱등). */
    RiskProfileRecalculateResponseDTO recalculateRiskProfile(Long userId);

    /** 공동 목표 취소 (soft delete). */
    GoalCancelResponseDTO cancelGoal(Long userId);
}
