package com.moait.moai.domain.goal.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** {@code targetDate} 파생 투자기간(개월). */
public final class InvestmentPeriod {

    private InvestmentPeriod() {
    }

    public static int monthsUntil(LocalDate targetDate) {
        return monthsBetween(LocalDate.now(), targetDate);
    }

    static int monthsBetween(LocalDate from, LocalDate targetDate) {
        long months = ChronoUnit.MONTHS.between(from, targetDate);
        return (int) Math.max(months, 0);
    }
}
