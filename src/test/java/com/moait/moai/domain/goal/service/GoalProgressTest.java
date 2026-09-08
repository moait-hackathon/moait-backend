package com.moait.moai.domain.goal.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoalProgressTest {

    @Test
    @DisplayName("rate / remainingAmount / requiredMonthlyAmount 계산")
    void basic() {
        LocalDate target = LocalDate.now().plusMonths(60);
        GoalProgress p = GoalProgress.of(100_000_000L, 20_000_000L, target);

        assertThat(p.rate()).isEqualByComparingTo(BigDecimal.valueOf(20.0));
        assertThat(p.remainingAmount()).isEqualTo(80_000_000L);
        assertThat(p.remainingMonths()).isEqualTo(60);
        assertThat(p.requiredMonthlyAmount()).isEqualTo(1_333_334L); // ceil(80,000,000 / 60)
    }

    @Test
    @DisplayName("목표 초과 달성 시 남은 금액 0, rate 100 초과 허용")
    void overachieved() {
        GoalProgress p = GoalProgress.of(100_000_000L, 120_000_000L, LocalDate.now().plusMonths(10));

        assertThat(p.remainingAmount()).isZero();
        assertThat(p.requiredMonthlyAmount()).isZero();
        assertThat(p.rate()).isEqualByComparingTo(BigDecimal.valueOf(120.0));
    }

    @Test
    @DisplayName("목표일이 지났으면 remainingMonths 0, requiredMonthlyAmount = 남은 금액 전액")
    void pastTargetDate() {
        GoalProgress p = GoalProgress.of(100_000_000L, 30_000_000L, LocalDate.now().minusDays(1));

        assertThat(p.remainingMonths()).isZero();
        assertThat(p.requiredMonthlyAmount()).isEqualTo(70_000_000L);
    }
}
