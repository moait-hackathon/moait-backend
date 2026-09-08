package com.moait.moai.domain.home.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HomeResponseDTO(
        String userName,
        String coupleName,
        LocalDate targetDate,
        Long targetAmount,
        Long currentAmount,
        BigDecimal achievementRate,
        BigDecimal totalAssetReturnRate,
        BigDecimal totalAssetChangeAmount,
        List<AssetReturnRateGraphDTO> assetReturnRateGraph,
        BigDecimal todayAssetReturnRate,
        BigDecimal todayAssetChangeAmount
) {
}
