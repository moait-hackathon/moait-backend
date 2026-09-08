package com.moait.moai.domain.goal.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.moait.moai.common.enums.CoupleStatus;
import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.dto.GoalOnboardingRequestDTO;
import com.moait.moai.domain.goal.dto.GoalResponseDTO;
import com.moait.moai.domain.goal.dto.GoalUpdateRequestDTO;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoalServiceImplTest {

    private static final long USER = 1L;
    private static final long COUPLE_ID = 200L;

    @Mock
    private GoalRepository goalRepository;
    @Mock
    private CoupleRepository coupleRepository;

    private final RiskProfileCalculator riskProfileCalculator = new RiskProfileCalculator();

    private GoalServiceImpl service;
    private Couple couple;

    @BeforeEach
    void setUp() {
        service = new GoalServiceImpl(goalRepository, coupleRepository, riskProfileCalculator);
        couple = Couple.createWait(USER, 2L);
        couple.connect();
        setId(couple, COUPLE_ID);
        lenient().when(coupleRepository.findConnectedByUserId(USER)).thenReturn(Optional.of(couple));
    }

    private GoalOnboardingRequestDTO onboarding(LocalDate targetDate, int maxLoss) {
        return new GoalOnboardingRequestDTO(
                100_000_000L, targetDate, 20_000_000L, 500_000L,
                EmergencyFundMonths.M3_6, MonthlySurplusBand.B20_30,
                maxLoss, LossReaction.HOLD, InvestmentExperience.ETF_ONLY);
    }

    @Test
    @DisplayName("온보딩 제출 → ACTIVE + R 산출 + onboardingStep DONE")
    void createHappy() {
        when(goalRepository.existsByCoupleId(COUPLE_ID)).thenReturn(false);
        when(goalRepository.saveAndFlush(any(Goal.class))).thenAnswer(inv -> inv.getArgument(0));

        GoalResponseDTO res = service.createGoal(USER, onboarding(LocalDate.now().plusMonths(60), 10));

        assertThat(res.status()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(res.investmentPeriodMonths()).isEqualTo(60);
        assertThat(res.riskProfileScore()).isEqualTo(62); // 배점표 합산
        assertThat(res.onboardingStep().name()).isEqualTo("DONE");
        assertThat(res.progress().remainingAmount()).isEqualTo(80_000_000L);
    }

    @Test
    @DisplayName("커플 연결 안 됨 → COUPLE_NOT_CONNECTED")
    void createNotConnected() {
        when(coupleRepository.findConnectedByUserId(USER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createGoal(USER, onboarding(LocalDate.now().plusMonths(12), 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.COUPLE_NOT_CONNECTED);
    }

    @Test
    @DisplayName("이미 목표 있음 → GOAL_ALREADY_EXISTS")
    void createDuplicate() {
        when(goalRepository.existsByCoupleId(COUPLE_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.createGoal(USER, onboarding(LocalDate.now().plusMonths(12), 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.GOAL_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("목표일 과거 → INVALID_TARGET_DATE")
    void createPastDate() {
        when(goalRepository.existsByCoupleId(COUPLE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createGoal(USER, onboarding(LocalDate.now().minusDays(1), 10)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_TARGET_DATE);
    }

    @Test
    @DisplayName("maxAllowedLossRate 비허용 값 → INVALID_INPUT")
    void createBadLossRate() {
        when(goalRepository.existsByCoupleId(COUPLE_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.createGoal(USER, onboarding(LocalDate.now().plusMonths(12), 13)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("조회 - 목표 없음 → GOAL_NOT_FOUND")
    void getMissing() {
        when(goalRepository.findByCoupleId(COUPLE_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMyGoal(USER))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.GOAL_NOT_FOUND);
    }

    @Test
    @DisplayName("수정 - 점수 문항 변경 시 R 재계산")
    void patchRecalculates() {
        Goal goal = existingGoal(LocalDate.now().plusMonths(60), 10, LossReaction.HOLD);
        when(goalRepository.findByCoupleId(COUPLE_ID)).thenReturn(Optional.of(goal));
        int before = goal.getRiskProfileScore();

        GoalUpdateRequestDTO req = new GoalUpdateRequestDTO(
                null, null, null, null, null, null, 40, LossReaction.BUY_MORE, null);
        GoalResponseDTO res = service.updateGoal(USER, req);

        assertThat(res.riskProfileScore()).isNotEqualTo(before);
        assertThat(res.maxAllowedLossRate()).isEqualTo(40);
        assertThat(res.lossReaction()).isEqualTo(LossReaction.BUY_MORE);
    }

    @Test
    @DisplayName("수정 - 취소된 목표 → GOAL_NOT_ACTIVE")
    void patchCancelled() {
        Goal goal = existingGoal(LocalDate.now().plusMonths(60), 10, LossReaction.HOLD);
        goal.cancel();
        when(goalRepository.findByCoupleId(COUPLE_ID)).thenReturn(Optional.of(goal));

        assertThatThrownBy(() -> service.updateGoal(USER, new GoalUpdateRequestDTO(
                200_000_000L, null, null, null, null, null, null, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.GOAL_NOT_ACTIVE);
    }

    @Test
    @DisplayName("현재 투자금 갱신 - 목표 도달 시 ACHIEVED")
    void currentAmountAchieved() {
        Goal goal = existingGoal(LocalDate.now().plusMonths(60), 10, LossReaction.HOLD);
        when(goalRepository.findByCoupleId(COUPLE_ID)).thenReturn(Optional.of(goal));

        var res = service.updateCurrentAmount(USER, 100_000_000L);

        assertThat(res.status()).isEqualTo(GoalStatus.ACHIEVED);
        assertThat(res.progress().remainingAmount()).isZero();
    }

    @Test
    @DisplayName("취소 - ACTIVE → CANCELLED")
    void cancelHappy() {
        Goal goal = existingGoal(LocalDate.now().plusMonths(60), 10, LossReaction.HOLD);
        when(goalRepository.findByCoupleId(COUPLE_ID)).thenReturn(Optional.of(goal));

        var res = service.cancelGoal(USER);

        assertThat(res.status()).isEqualTo(GoalStatus.CANCELLED);
    }

    private Goal existingGoal(LocalDate targetDate, int maxLoss, LossReaction lossReaction) {
        int period = InvestmentPeriod.monthsUntil(targetDate);
        RiskProfileCalculator.Result r = riskProfileCalculator.calculate(
                period, maxLoss, lossReaction, EmergencyFundMonths.M3_6,
                MonthlySurplusBand.B20_30, InvestmentExperience.ETF_ONLY);
        Goal goal = Goal.create(COUPLE_ID, 100_000_000L, targetDate, period, 20_000_000L, 500_000L,
                EmergencyFundMonths.M3_6, MonthlySurplusBand.B20_30, maxLoss, lossReaction,
                InvestmentExperience.ETF_ONLY, r.score(), r.type());
        setId(goal, 400L);
        return goal;
    }

    private static void setId(Object entity, long id) {
        try {
            var field = entity.getClass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(entity, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
