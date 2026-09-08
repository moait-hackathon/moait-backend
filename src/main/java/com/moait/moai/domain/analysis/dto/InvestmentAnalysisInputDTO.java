package com.moait.moai.domain.analysis.dto;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import java.math.BigDecimal;
import java.util.List;

/** DB 읽기 트랜잭션 안에서 확보한 분석 입력 스냅샷. A=male_id, B=female_id. */
public record InvestmentAnalysisInputDTO(
        Long goalId, GoalAnalysisRequestDTO goal, JointFund jointFund,
        List<AssetPosition> personA, List<AssetPosition> personB) {

    public record AssetPosition(String assetType, String riskLevel,
                                BigDecimal currentValue, String currencyCode) { }

    public record JointFund(Integer maxAllowedLossRate, LossReaction lossReaction,
                            EmergencyFundMonths emergencyFundBand, MonthlySurplusBand surplusBand,
                            InvestmentExperience investmentExperience) { }
}
