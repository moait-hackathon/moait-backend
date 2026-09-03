package com.moait.moai.domain.user.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.user.dto.FinancialInfoRequestDTO;
import com.moait.moai.domain.user.dto.OnboardingStepResponseDTO;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OnboardingServiceImpl implements OnboardingService {

    private final UserRepository userRepository;
    private final OnboardingStepResolver onboardingStepResolver;

    @Override
    @Transactional
    public OnboardingStepResponseDTO updateFinancialInfo(Long userId, FinancialInfoRequestDTO request) {
        User user = findUser(userId);
        user.updateFinancialInfo(request.annualIncome(), request.totalAsset());
        return OnboardingStepResponseDTO.of(onboardingStepResolver.resolve(user));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "사용자를 찾을 수 없습니다."));
    }
}
