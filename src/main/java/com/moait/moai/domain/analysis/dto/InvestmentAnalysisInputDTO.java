package com.moait.moai.domain.analysis.dto;

import java.math.BigDecimal;
import java.util.List;

/** DB 읽기 트랜잭션 안에서 확보한 분석 입력 스냅샷. A=male_id, B=female_id. */
public record InvestmentAnalysisInputDTO(
        Long goalId, GoalAnalysisRequestDTO goal, JointFund jointFund,
        List<AssetPosition> personA, List<AssetPosition> personB) {

    public record AssetPosition(String assetType, String riskLevel,
                                BigDecimal currentValue, String currencyCode) { }

    public record JointFund(Integer maxAllowedLossRate, LossReaction lossReaction,
                            EmergencyFundBand emergencyFundBand, SurplusBand surplusBand,
                            InvestmentExperience investmentExperience) { }

    public enum LossReaction { SELL_ALL, SELL_MOST, SELL_PART, HOLD, BUY_MORE }
    public enum EmergencyFundBand { UNDER_1M, M1_3, M3_6, M6_12, OVER_12M }
    public enum SurplusBand { UNDER_0, UNDER_10, B10_20, B20_30, OVER_30 }
    public enum InvestmentExperience { NONE, SAVINGS_ONLY, ETF_ONLY, STOCK_ALL, MULTI_ASSET }
}
