package com.moait.moai.domain.goal.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * 목표 진척률. 규칙은 {@code docs/api-spec.md} "progress 계산 규칙".
 *
 * <pre>
 * rate                  = round(currentAmount / targetAmount * 100, 1)
 * remainingAmount       = max(targetAmount - currentAmount, 0)
 * remainingMonths       = 오늘 ~ targetDate 개월수
 * requiredMonthlyAmount = remainingMonths > 0 ? ceil(remainingAmount / remainingMonths) : remainingAmount
 * </pre>
 */
public record GoalProgress(
        BigDecimal rate,
        long remainingAmount,
        int remainingMonths,
        long requiredMonthlyAmount
) {

    public static GoalProgress of(long targetAmount, long currentAmount, LocalDate targetDate) {
        BigDecimal rate = targetAmount <= 0
                ? BigDecimal.ZERO.setScale(1, RoundingMode.UNNECESSARY)
                : BigDecimal.valueOf(currentAmount)
                        .divide(BigDecimal.valueOf(targetAmount), 6, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(1, RoundingMode.HALF_UP);

        long remainingAmount = Math.max(targetAmount - currentAmount, 0);
        int remainingMonths = InvestmentPeriod.monthsUntil(targetDate);
        long requiredMonthlyAmount = remainingMonths > 0
                ? ceilDiv(remainingAmount, remainingMonths)
                : remainingAmount;

        return new GoalProgress(rate, remainingAmount, remainingMonths, requiredMonthlyAmount);
    }

    private static long ceilDiv(long amount, long months) {
        return (amount + months - 1) / months;
    }
}
