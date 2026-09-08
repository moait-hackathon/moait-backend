package com.moait.moai.domain.goal.dto;

import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.domain.goal.entity.Goal;

public record GoalCancelResponseDTO(Long goalId, GoalStatus status) {

    public static GoalCancelResponseDTO of(Goal goal) {
        return new GoalCancelResponseDTO(goal.getId(), goal.getStatus());
    }
}
