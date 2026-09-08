package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.InvestmentAgreementRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;

public interface InvestmentAnalysisService {

    InvestmentAgreementResponseDTO analyze(InvestmentAgreementRequestDTO request);
}
