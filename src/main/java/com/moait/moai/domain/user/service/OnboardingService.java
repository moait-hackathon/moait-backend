package com.moait.moai.domain.user.service;

import com.moait.moai.domain.user.dto.FinancialInfoRequestDTO;
import com.moait.moai.domain.user.dto.InvestmentProfileRequestDTO;
import com.moait.moai.domain.user.dto.InvestmentProfileResponseDTO;
import com.moait.moai.domain.user.dto.OnboardingStepResponseDTO;

public interface OnboardingService {

    /** 온보딩 - 기본 재무정보(연소득/총자산) 입력. */
    OnboardingStepResponseDTO updateFinancialInfo(Long userId, FinancialInfoRequestDTO request);

    /** 온보딩 - 투자성향 설문 제출. 점수/유형을 산출해 저장하고 재설문 시 이전 프로필을 최신 해제한다. */
    InvestmentProfileResponseDTO submitInvestmentProfile(Long userId, InvestmentProfileRequestDTO request);
}
