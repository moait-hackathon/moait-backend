package com.moait.moai.domain.home.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AssetReturnRateGraphDTO(
        LocalDate date,
        BigDecimal returnRate
) {
}
