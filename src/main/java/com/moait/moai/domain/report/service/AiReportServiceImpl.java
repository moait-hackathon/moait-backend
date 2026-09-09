package com.moait.moai.domain.report.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import com.moait.moai.domain.report.dto.AiReportResponseDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.AdjustmentOptionDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.AgreedProfileDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.AgreementDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.GoalDiagnosisDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.GoalRequirementDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.HeadlineDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.MetaDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.PersonRiskDTO;
import com.moait.moai.domain.report.dto.AiReportResponseDTO.RecommendationDTO;
import com.moait.moai.domain.report.entity.InvestmentReport;
import com.moait.moai.domain.report.repository.InvestmentReportRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiReportServiceImpl implements AiReportService {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final GoalRepository goalRepository;
    private final InvestmentReportRepository reportRepository;

    @Override
    @Transactional(readOnly = true)
    public AiReportResponseDTO getMyReport(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사용자를 찾을 수 없습니다."));
        Couple couple = coupleRepository.findConnectedByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "연결된 커플을 찾을 수 없습니다."));
        Goal goal = goalRepository.findByCoupleId(couple.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "공동 목표를 찾을 수 없습니다."));
        InvestmentReport report = reportRepository.findFirstByGoalIdOrderByCreatedAtDesc(goal.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "AI 리포트를 찾을 수 없습니다."));

        return toResponse(user, couple, goal, report);
    }

    private AiReportResponseDTO toResponse(User user, Couple couple, Goal goal, InvestmentReport report) {
        String status = report.getStatus();
        String summary = report.getSummaryMessage();
        String tone = "UNSUITABLE".equals(status) ? "WARNING" : "POSITIVE";
        String title = "UNSUITABLE".equals(status) ? "목표 조정이\n필요해요!" : "투자 계획이\n적정해요!";
        Integer score = report.getRecommendedRiskScore();
        String profileType = goal.getJointRiskProfileType() == null
                ? null : goal.getJointRiskProfileType().name();

        return new AiReportResponseDTO(
                new MetaDTO(report.getCreatedAt() == null ? null : report.getCreatedAt().toLocalDate(),
                        report.getInputTargetAmount(), report.getInvestmentMonths()),
                new HeadlineDTO(tone, title, title.replace("\n", " "), summary,
                        summary, null, report.getRationale()),
                null,
                null,
                jointFund(goal, profileType),
                new AgreedProfileDTO(goal.getRiskProfileScore(), profileType,
                        profileLabel(goal.getJointRiskProfileType()), report.getRationale()),
                new RecommendationDTO(score, score, null, null, score),
                new GoalRequirementDTO(null, null, null, !"UNSUITABLE".equals(status),
                        report.getInvestmentMonths(), report.getCalculationMethod()),
                new AgreementDTO(status, score, summary, report.getRationale(),
                        report.getRecommendedStrategy(), adjustments(report.getGoalAdjustments()),
                        List.of(), null),
                null,
                null,
                adjustmentOptions(report.getGoalAdjustments()),
                new GoalDiagnosisDTO(report.getInputCurrentAmount(), report.getInputTargetAmount(), null, null),
                null,
                null,
                null,
                null,
                null,
                "본 리포트의 예상 금액과 수익률은 입력된 목표와 가정에 따른 참고용 계산 결과입니다.");
    }

    private PersonRiskDTO jointFund(Goal goal, String profileType) {
        Integer score = goal.getRiskProfileScore();
        return new PersonRiskDTO(score, null, null, score, profileType);
    }

    private String profileLabel(RiskProfileType type) {
        return type == null ? null : type.getLabel();
    }

    private List<String> adjustments(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("\\R"))
                .filter(value -> !value.isBlank())
                .toList();
    }

    private List<AdjustmentOptionDTO> adjustmentOptions(String raw) {
        return adjustments(raw).stream()
                .map(value -> new AdjustmentOptionDTO(typeOf(value), value, null, null, null))
                .toList();
    }

    private String typeOf(String value) {
        if (value.contains("월 납입액") || value.contains("월 투자액")) {
            return "MONTHLY_CONTRIBUTION";
        }
        if (value.contains("목표일") || value.contains("기간")) {
            return "TARGET_DATE";
        }
        if (value.contains("목표금액")) {
            return "TARGET_AMOUNT";
        }
        return null;
    }
}
