package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO;

public interface InvestmentAnalysisInputService {
    InvestmentAnalysisInputDTO load(Long userId);
}
