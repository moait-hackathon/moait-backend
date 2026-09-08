package com.moait.moai.domain.analysis.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.AssetPosition;
import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.JointFund;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import com.moait.moai.domain.asset.repository.InvestmentAssetRepository;
import com.moait.moai.domain.asset.repository.InvestmentAssetRepository.PortfolioPosition;
import com.moait.moai.domain.goal.repository.GoalRepository;
import jakarta.validation.Validator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentAnalysisInputServiceImpl implements InvestmentAnalysisInputService {
    private final GoalRepository goalRepository;
    private final InvestmentAssetRepository assetRepository;
    private final Validator validator;

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public InvestmentAnalysisInputDTO load(Long userId) {
        var goals = goalRepository.findConnectedGoalsByUserId(userId);
        if (goals.isEmpty()) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "연결된 커플의 공동 목표를 찾을 수 없습니다.");
        }
        if (goals.size() != 1) {
            throw new InvestmentAnalysisDataException("연결된 공동 목표가 여러 개입니다. 커플 연결 상태를 확인해 주세요.");
        }
        var stored = goals.getFirst();
        if (!stored.isActive()) {
            throw new InvestmentAnalysisDataException("진행 중인 공동 목표만 분석할 수 있습니다.");
        }
        var goal = new GoalAnalysisRequestDTO(stored.getTargetAmount(), stored.getCurrentAmount(),
                stored.getMonthlyInvestableAmount(), stored.getTargetDate());
        var violations = validator.validate(goal);
        if (!violations.isEmpty()) {
            String fields = violations.stream().map(v -> v.getPropertyPath().toString())
                    .sorted().collect(Collectors.joining(", "));
            throw new InvestmentAnalysisDataException("공동 목표의 저장 값을 확인해 주세요: " + fields);
        }
        Integer loss = stored.getMaxAllowedLossRate();
        if (loss == null || !Set.of(0, 5, 10, 20, 30, 40).contains(loss)) {
            throw new InvestmentAnalysisDataException("공동 목표의 max_allowed_loss_rate를 확인해 주세요.");
        }
        var joint = new JointFund(loss,
                requireAnswer(stored.getLossReaction(), "loss_reaction"),
                requireAnswer(stored.getEmergencyFundMonths(), "emergency_fund_months"),
                requireAnswer(stored.getMonthlySurplusBand(), "monthly_surplus_band"),
                requireAnswer(stored.getInvestmentExperience(), "investment_experience"));
        var positions = assetRepository.findActivePositionsByCoupleId(stored.getCoupleId());
        return new InvestmentAnalysisInputDTO(stored.getId(), goal, joint,
                positionsFor(positions, "A"), positionsFor(positions, "B"));
    }

    private List<AssetPosition> positionsFor(List<PortfolioPosition> positions, String person) {
        return positions.stream().filter(p -> person.equals(p.getPerson()))
                .map(p -> new AssetPosition(p.getAssetType(), p.getRiskLevel(),
                        p.getCurrentValue(), p.getCurrencyCode())).toList();
    }

    private <E extends Enum<E>> E requireAnswer(E value, String field) {
        if (value == null) {
            throw new InvestmentAnalysisDataException("공동 목표의 " + field + " 입력을 완료해 주세요.");
        }
        return value;
    }
}
