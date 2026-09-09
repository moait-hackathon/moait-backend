package com.moait.moai.domain.report.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import com.moait.moai.domain.report.entity.InvestmentReport;
import com.moait.moai.domain.report.repository.InvestmentReportRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AiReportServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CoupleRepository coupleRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private InvestmentReportRepository reportRepository;
    @InjectMocks
    private AiReportServiceImpl aiReportService;

    @Test
    void 최신리포트를_프론트응답형태로_반환한다() {
        User user = mock(User.class);
        Couple couple = mock(Couple.class);
        Goal goal = mock(Goal.class);
        InvestmentReport report = mock(InvestmentReport.class);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(couple.getId()).thenReturn(10L);
        when(coupleRepository.findConnectedByUserId(USER_ID)).thenReturn(Optional.of(couple));
        when(goalRepository.findByCoupleId(10L)).thenReturn(Optional.of(goal));
        when(goal.getRiskProfileScore()).thenReturn(45);
        when(goal.getJointRiskProfileType()).thenReturn(RiskProfileType.STABLE_SEEKING);
        when(reportRepository.findFirstByGoalIdOrderByCreatedAtDesc(goal.getId()))
                .thenReturn(Optional.of(report));
        when(report.getCreatedAt()).thenReturn(LocalDateTime.of(2026, 9, 8, 12, 0));
        when(report.getInputTargetAmount()).thenReturn(195_000_000L);
        when(report.getInputCurrentAmount()).thenReturn(50_000_000L);
        when(report.getInvestmentMonths()).thenReturn(24);
        when(report.getStatus()).thenReturn("UNSUITABLE");
        when(report.getSummaryMessage()).thenReturn("목표 조정이 필요합니다.");
        when(report.getRationale()).thenReturn("현재 계획을 조정해야 합니다.");
        when(report.getRecommendedRiskScore()).thenReturn(null);
        when(report.getRecommendedStrategy()).thenReturn(null);
        when(report.getGoalAdjustments()).thenReturn("월 납입액을 늘리세요.\n목표일을 늦추세요.");
        when(report.getCalculationMethod()).thenReturn("LEGACY_RETURN_BANDS");

        var result = aiReportService.getMyReport(USER_ID);

        assertThat(result.meta().reportDate()).isEqualTo(LocalDate.of(2026, 9, 8));
        assertThat(result.meta().targetAmount()).isEqualTo(195_000_000L);
        assertThat(result.agreement().status()).isEqualTo("UNSUITABLE");
        assertThat(result.agreement().alternatives()).hasSize(2);
        assertThat(result.agreedProfile().score()).isEqualTo(45);
        assertThat(result.agreedProfile().profileType()).isEqualTo("STABLE_SEEKING");
        assertThat(result.goalDiagnosis().currentAmount()).isEqualTo(50_000_000L);
    }

    @Test
    void 리포트가_없으면_예외를_반환한다() {
        User user = mock(User.class);
        Couple couple = mock(Couple.class);
        Goal goal = mock(Goal.class);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(coupleRepository.findConnectedByUserId(USER_ID)).thenReturn(Optional.of(couple));
        when(couple.getId()).thenReturn(10L);
        when(goalRepository.findByCoupleId(10L)).thenReturn(Optional.of(goal));
        when(goal.getId()).thenReturn(20L);
        when(reportRepository.findFirstByGoalIdOrderByCreatedAtDesc(20L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> aiReportService.getMyReport(USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.RESOURCE_NOT_FOUND);
    }
}
