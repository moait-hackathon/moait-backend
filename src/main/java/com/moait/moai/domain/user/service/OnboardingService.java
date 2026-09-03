package com.moait.moai.domain.user.service;

import com.moait.moai.domain.user.dto.FinancialInfoRequestDTO;
import com.moait.moai.domain.user.dto.OnboardingStepResponseDTO;

public interface OnboardingService {

    /** 온보딩 - 기본 재무정보(연소득/총자산) 입력. */
    OnboardingStepResponseDTO updateFinancialInfo(Long userId, FinancialInfoRequestDTO request);
}
