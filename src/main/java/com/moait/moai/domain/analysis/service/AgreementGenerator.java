package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Agreement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;

public interface AgreementGenerator {

    Agreement generate(Recommendation recommendation, GoalRequirement goalRequirement,
                       GoalAnalysisRequestDTO goal, String deterministicStatus);
}
