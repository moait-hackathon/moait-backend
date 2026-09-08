package com.moait.moai.domain.goal.service;

import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.common.enums.OnboardingStep;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.dto.CurrentAmountResponseDTO;
import com.moait.moai.domain.goal.dto.GoalCancelResponseDTO;
import com.moait.moai.domain.goal.dto.GoalOnboardingRequestDTO;
import com.moait.moai.domain.goal.dto.GoalResponseDTO;
import com.moait.moai.domain.goal.dto.GoalUpdateRequestDTO;
import com.moait.moai.domain.goal.dto.RiskProfileRecalculateResponseDTO;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import java.time.LocalDate;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GoalServiceImpl implements GoalService {

    private static final Set<Integer> ALLOWED_LOSS_RATES = Set.of(0, 5, 10, 20, 30, 40);

    private final GoalRepository goalRepository;
    private final CoupleRepository coupleRepository;
    private final RiskProfileCalculator riskProfileCalculator;

    @Override
    @Transactional
    public GoalResponseDTO createGoal(Long userId, GoalOnboardingRequestDTO req) {
        Couple couple = coupleRepository.findConnectedByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COUPLE_NOT_CONNECTED));
        if (goalRepository.existsByCoupleId(couple.getId())) {
            throw new BusinessException(ErrorCode.GOAL_ALREADY_EXISTS);
        }
        validateTargetDate(req.targetDate());
        validateLossRate(req.maxAllowedLossRate());

        int period = InvestmentPeriod.monthsUntil(req.targetDate());
        RiskProfileCalculator.Result r = riskProfileCalculator.calculate(
                period, req.maxAllowedLossRate(), req.lossReaction(),
                req.emergencyFundMonths(), req.monthlySurplusBand(), req.investmentExperience());

        Goal goal = Goal.create(couple.getId(), req.targetAmount(), req.targetDate(), period,
                req.currentAmount(), req.monthlyInvestableAmount(), req.emergencyFundMonths(),
                req.monthlySurplusBand(), req.maxAllowedLossRate(), req.lossReaction(),
                req.investmentExperience(), r.score(), r.type());
        try {
            goalRepository.saveAndFlush(goal);
        } catch (DataIntegrityViolationException e) {
            // 두 파트너 동시 제출 — UNIQUE(couple_id) 위반
            throw new BusinessException(ErrorCode.GOAL_ALREADY_EXISTS);
        }
        return toResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public GoalResponseDTO getMyGoal(Long userId) {
        return toResponse(findGoal(userId));
    }

    @Override
    @Transactional
    public GoalResponseDTO updateGoal(Long userId, GoalUpdateRequestDTO req) {
        Goal goal = findGoal(userId);
        if (goal.isCancelled()) {
            throw new BusinessException(ErrorCode.GOAL_NOT_ACTIVE);
        }
        if (req.targetDate() != null) {
            validateTargetDate(req.targetDate());
        }
        if (req.maxAllowedLossRate() != null) {
            validateLossRate(req.maxAllowedLossRate());
        }

        goal.updatePlan(req.targetAmount(), req.targetDate(), req.currentAmount(),
                req.monthlyInvestableAmount());
        goal.updateAnswers(req.emergencyFundMonths(), req.monthlySurplusBand(), req.maxAllowedLossRate(),
                req.lossReaction(), req.investmentExperience());
        goal.reactivate();

        if (req.touchesRiskInputs()) {
            recalculate(goal);
        }
        return toResponse(goal);
    }

    @Override
    @Transactional
    public CurrentAmountResponseDTO updateCurrentAmount(Long userId, long currentAmount) {
        Goal goal = findGoal(userId);
        if (goal.isCancelled()) {
            throw new BusinessException(ErrorCode.GOAL_NOT_ACTIVE);
        }
        goal.updateCurrentAmount(currentAmount);
        return CurrentAmountResponseDTO.of(goal, progressOf(goal));
    }

    @Override
    @Transactional
    public RiskProfileRecalculateResponseDTO recalculateRiskProfile(Long userId) {
        Goal goal = findGoal(userId);
        recalculate(goal);
        return RiskProfileRecalculateResponseDTO.of(goal);
    }

    @Override
    @Transactional
    public GoalCancelResponseDTO cancelGoal(Long userId) {
        Goal goal = findGoal(userId);
        if (!goal.isActive()) {
            throw new BusinessException(ErrorCode.GOAL_NOT_ACTIVE);
        }
        goal.cancel();
        return GoalCancelResponseDTO.of(goal);
    }

    private Goal findGoal(Long userId) {
        Couple couple = coupleRepository.findConnectedByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
        return goalRepository.findByCoupleId(couple.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.GOAL_NOT_FOUND));
    }

    private void recalculate(Goal goal) {
        int period = InvestmentPeriod.monthsUntil(goal.getTargetDate());
        RiskProfileCalculator.Result r = riskProfileCalculator.calculate(
                period, goal.getMaxAllowedLossRate(), goal.getLossReaction(),
                goal.getEmergencyFundMonths(), goal.getMonthlySurplusBand(), goal.getInvestmentExperience());
        goal.applyRiskProfile(period, r.score(), r.type());
    }

    private GoalProgress progressOf(Goal goal) {
        long current = goal.getCurrentAmount() == null ? 0L : goal.getCurrentAmount();
        long target = goal.getTargetAmount() == null ? 0L : goal.getTargetAmount();
        return GoalProgress.of(target, current, goal.getTargetDate());
    }

    private GoalResponseDTO toResponse(Goal goal) {
        OnboardingStep step = goal.getStatus() == GoalStatus.CANCELLED
                ? OnboardingStep.GOAL_ONBOARDING
                : OnboardingStep.DONE;
        // TODO(report): hasReport — investment_report 도메인 구현 후 존재 여부 조회
        return GoalResponseDTO.of(goal, progressOf(goal), false, step);
    }

    private void validateTargetDate(LocalDate targetDate) {
        if (!targetDate.isAfter(LocalDate.now())) {
            throw new BusinessException(ErrorCode.INVALID_TARGET_DATE);
        }
    }

    private void validateLossRate(int rate) {
        if (!ALLOWED_LOSS_RATES.contains(rate)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT,
                    "maxAllowedLossRate 는 0 / 5 / 10 / 20 / 30 / 40 중 하나여야 합니다.");
        }
    }
}
