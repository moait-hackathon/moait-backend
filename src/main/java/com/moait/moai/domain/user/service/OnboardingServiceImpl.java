package com.moait.moai.domain.user.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.user.dto.FinancialInfoRequestDTO;
import com.moait.moai.domain.user.dto.InvestmentProfileRequestDTO;
import com.moait.moai.domain.user.dto.InvestmentProfileRequestDTO.HoldingAssetItem;
import com.moait.moai.domain.user.dto.InvestmentProfileResponseDTO;
import com.moait.moai.domain.user.dto.OnboardingStepResponseDTO;
import com.moait.moai.domain.user.entity.HoldingAsset;
import com.moait.moai.domain.user.entity.InvestmentProfile;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.InvestmentProfileRepository;
import com.moait.moai.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final InvestmentProfileRepository investmentProfileRepository;
    private final OnboardingStepResolver onboardingStepResolver;
    private final RiskProfileCalculator riskProfileCalculator;

    @Override
    @Transactional
    public OnboardingStepResponseDTO updateFinancialInfo(Long userId, FinancialInfoRequestDTO request) {
        User user = findUser(userId);
        user.updateFinancialInfo(request.annualIncome(), request.totalAsset());
        return OnboardingStepResponseDTO.of(onboardingStepResolver.resolve(user));
    }

    @Override
    @Transactional
    public InvestmentProfileResponseDTO submitInvestmentProfile(Long userId,
                                                               InvestmentProfileRequestDTO request) {
        User user = findUser(userId);

        RiskProfileCalculator.Result result = riskProfileCalculator.calculate(
                request.investmentExperience(),
                request.lossReaction(),
                request.maxTolerableLossRate(),
                request.investmentHorizon(),
                request.emergencyFundSecured());

        investmentProfileRepository.markAllStale(userId);

        InvestmentProfile saved = investmentProfileRepository.save(InvestmentProfile.create(
                userId,
                request.investmentExperience(),
                request.lossReaction(),
                request.maxTolerableLossRate(),
                request.investmentHorizon(),
                toHoldingAssets(request.holdingAssets()),
                request.monthlyInvestableAmount(),
                request.emergencyFundSecured(),
                result.type(),
                result.score()));

        return InvestmentProfileResponseDTO.of(saved, onboardingStepResolver.resolve(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }

    private List<HoldingAsset> toHoldingAssets(List<HoldingAssetItem> items) {
        return items.stream()
                .map(item -> new HoldingAsset(item.type(), item.amount()))
                .toList();
    }
}
