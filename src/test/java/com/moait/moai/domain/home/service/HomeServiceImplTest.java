package com.moait.moai.domain.home.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import com.moait.moai.domain.home.entity.HomeInvestmentAccount;
import com.moait.moai.domain.home.entity.HomeInvestmentAsset;
import com.moait.moai.domain.home.entity.HomeInvestmentAssetSnapshot;
import com.moait.moai.domain.home.repository.InvestmentAccountRepository;
import com.moait.moai.domain.home.repository.HomeInvestmentAssetRepository;
import com.moait.moai.domain.home.repository.InvestmentAssetSnapshotRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HomeServiceImplTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CoupleRepository coupleRepository;
    @Mock
    private GoalRepository goalRepository;
    @Mock
    private InvestmentAccountRepository accountRepository;
    @Mock
    private HomeInvestmentAssetRepository assetRepository;
    @Mock
    private InvestmentAssetSnapshotRepository snapshotRepository;
    @InjectMocks
    private HomeServiceImpl homeService;

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        when(user.getName()).thenReturn("사용자");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
    }

    @Test
    void 목표와커플정보를_홈응답으로_반환한다() {
        Couple couple = mock(Couple.class);
        User partner = mock(User.class);
        Goal goal = mock(Goal.class);

        when(couple.getId()).thenReturn(10L);
        when(couple.partnerOf(USER_ID)).thenReturn(2L);
        when(coupleRepository.findConnectedByUserId(USER_ID)).thenReturn(Optional.of(couple));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));
        when(partner.getName()).thenReturn("커플");
        when(goalRepository.findByCoupleId(10L)).thenReturn(Optional.of(goal));
        when(goal.getTargetDate()).thenReturn(LocalDate.of(2030, 1, 1));
        when(goal.getTargetAmount()).thenReturn(100_000_000L);
        when(goal.getCurrentAmount()).thenReturn(25_000_000L);
        when(accountRepository.findAllByUserIdInAndActiveTrue(List.of(USER_ID, 2L))).thenReturn(List.of());

        var result = homeService.getHome(USER_ID);

        assertThat(result.userName()).isEqualTo("사용자");
        assertThat(result.coupleName()).isEqualTo("커플");
        assertThat(result.achievementRate()).isEqualByComparingTo("25.0");
        assertThat(result.targetAmount()).isEqualTo(100_000_000L);
        assertThat(result.currentAmount()).isEqualTo(25_000_000L);
    }

    @Test
    void 자산스냅샷을_날짜별그래프와_최신수익률로_집계한다() {
        HomeInvestmentAccount account = mock(HomeInvestmentAccount.class);
        HomeInvestmentAsset asset = mock(HomeInvestmentAsset.class);
        HomeInvestmentAssetSnapshot first = mock(HomeInvestmentAssetSnapshot.class);
        HomeInvestmentAssetSnapshot latest = mock(HomeInvestmentAssetSnapshot.class);
        Couple couple = mock(Couple.class);
        User partner = mock(User.class);

        when(account.getId()).thenReturn(20L);
        when(asset.getId()).thenReturn(30L);
        when(asset.getPrincipalAmount()).thenReturn(new BigDecimal("1000"));
        when(asset.getEvaluationProfitLoss()).thenReturn(new BigDecimal("100"));
        when(couple.partnerOf(USER_ID)).thenReturn(2L);
        when(coupleRepository.findConnectedByUserId(USER_ID)).thenReturn(Optional.of(couple));
        when(userRepository.findById(2L)).thenReturn(Optional.of(partner));
        when(accountRepository.findAllByUserIdInAndActiveTrue(List.of(USER_ID, 2L)))
                .thenReturn(List.of(account));
        when(assetRepository.findAllByInvestmentAccountIdInAndActiveTrue(List.of(20L)))
                .thenReturn(List.of(asset));

        when(first.getSnapshotDate()).thenReturn(LocalDate.of(2026, 1, 1));
        when(first.getPrincipalAmount()).thenReturn(new BigDecimal("1000"));
        when(first.getEvaluationProfitLoss()).thenReturn(new BigDecimal("50"));
        when(latest.getSnapshotDate()).thenReturn(LocalDate.of(2026, 1, 2));
        when(latest.getPrincipalAmount()).thenReturn(new BigDecimal("1000"));
        when(latest.getEvaluationProfitLoss()).thenReturn(new BigDecimal("100"));
        when(snapshotRepository.findAllByInvestmentAssetIdInOrderBySnapshotDateAsc(List.of(30L)))
                .thenReturn(List.of(latest, first));
        var result = homeService.getHome(USER_ID);

        assertThat(result.totalAssetReturnRate()).isEqualByComparingTo("10.0");
        assertThat(result.totalAssetChangeAmount()).isEqualByComparingTo("100");
        assertThat(result.assetReturnRateGraph()).extracting(graph -> graph.date())
                .containsExactly(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2));
        assertThat(result.assetReturnRateGraph().get(0).returnRate()).isEqualByComparingTo("5.0");
        assertThat(result.todayAssetReturnRate()).isEqualByComparingTo("10.0");
        assertThat(result.todayAssetChangeAmount()).isEqualByComparingTo("100");
    }
}
