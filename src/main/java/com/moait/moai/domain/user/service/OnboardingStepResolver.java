package com.moait.moai.domain.user.service;

import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.InvestmentProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 사용자의 다음 온보딩 단계를 판정한다.
 *
 * <pre>
 * 재무정보(income/asset) 없음        → FINANCIAL_INFO
 * 최신 투자성향 설문 없음             → INVESTMENT_PROFILE
 * 둘 다 완료                        → DONE
 * </pre>
 */
@Component
@RequiredArgsConstructor
public class OnboardingStepResolver {

    private final InvestmentProfileRepository investmentProfileRepository;

    public OnboardingStep resolve(User user) {
        if (!user.hasFinancialInfo()) {
            return OnboardingStep.FINANCIAL_INFO;
        }
        if (!investmentProfileRepository.existsByUserIdAndIsLatestTrue(user.getId())) {
            return OnboardingStep.INVESTMENT_PROFILE;
        }
        return OnboardingStep.DONE;
    }
}
