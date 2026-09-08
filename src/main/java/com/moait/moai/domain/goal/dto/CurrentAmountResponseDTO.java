package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.service.GoalProgress;

public record CurrentAmountResponseDTO(
        Long goalId,
        Long currentAmount,
        java.time.LocalDateTime currentAmountUpdatedAt,
        GoalStatus status,
        GoalProgress progress
) {

    public static CurrentAmountResponseDTO of(Goal goal, GoalProgress progress) {
        return new CurrentAmountResponseDTO(
                goal.getId(), goal.getCurrentAmount(), goal.getCurrentAmountUpdatedAt(),
                goal.getStatus(), progress);
    }
}
