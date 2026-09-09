package com.moait.moai.domain.home.service;

import com.moait.moai.common.exception.BusinessException;
import com.moait.moai.common.exception.ErrorCode;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.couple.repository.CoupleRepository;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.goal.repository.GoalRepository;
import com.moait.moai.domain.home.dto.AssetReturnRateGraphDTO;
import com.moait.moai.domain.home.dto.HomeResponseDTO;
import com.moait.moai.domain.home.entity.HomeInvestmentAccount;
import com.moait.moai.domain.home.entity.HomeInvestmentAsset;
import com.moait.moai.domain.home.entity.HomeInvestmentAssetSnapshot;
import com.moait.moai.domain.home.repository.InvestmentAccountRepository;
import com.moait.moai.domain.home.repository.HomeInvestmentAssetRepository;
import com.moait.moai.domain.home.repository.InvestmentAssetSnapshotRepository;
import com.moait.moai.domain.user.entity.User;
import com.moait.moai.domain.user.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HomeServiceImpl implements HomeService {

    private static final int RATE_SCALE = 1;
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final BigDecimal ZERO_RATE = BigDecimal.ZERO.setScale(RATE_SCALE);

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final GoalRepository goalRepository;
    private final InvestmentAccountRepository investmentAccountRepository;
    private final HomeInvestmentAssetRepository investmentAssetRepository;
    private final InvestmentAssetSnapshotRepository snapshotRepository;

    @Override
    @Transactional(readOnly = true)
    public HomeResponseDTO getHome(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.RESOURCE_NOT_FOUND,
                        "사용자를 찾을 수 없습니다."));

        Couple couple = coupleRepository.findConnectedByUserId(userId).orElse(null);
        Goal goal = couple == null ? null : goalRepository.findByCoupleId(couple.getId()).orElse(null);
        User partner = couple == null ? null : userRepository.findById(couple.partnerOf(userId)).orElse(null);

        List<HomeInvestmentAsset> assets = findActiveAssets(userId, couple);
        AssetSummary assetSummary = summarizeAssets(assets);
        Long currentAmount = currentAmountOf(goal, assetSummary.changeAmount());

        return new HomeResponseDTO(
                user.getName(),
                partner == null ? null : partner.getName(),
                goal == null ? null : goal.getTargetDate(),
                goal == null ? null : goal.getTargetAmount(),
                currentAmount,
                achievementRate(goal, currentAmount),
                assetSummary.returnRate(),
                assetSummary.changeAmount(),
                assetSummary.graph(),
                assetSummary.todayReturnRate(),
                assetSummary.todayChangeAmount());
    }

    private List<HomeInvestmentAsset> findActiveAssets(Long userId, Couple couple) {
        List<Long> userIds = couple == null
                ? List.of(userId)
                : List.of(userId, couple.partnerOf(userId));
        List<HomeInvestmentAccount> accounts = investmentAccountRepository.findAllByUserIdInAndActiveTrue(userIds);
        if (accounts.isEmpty()) {
            return List.of();
        }
        return investmentAssetRepository.findAllByInvestmentAccountIdInAndActiveTrue(
                accounts.stream().map(HomeInvestmentAccount::getId).toList());
    }

    private Long currentAmountOf(Goal goal, BigDecimal assetChangeAmount) {
        if (goal == null || goal.getCurrentAmount() == null) {
            return null;
        }
        return BigDecimal.valueOf(goal.getCurrentAmount())
                .add(assetChangeAmount)
                .setScale(0, RoundingMode.HALF_UP)
                .longValue();
    }

    private BigDecimal achievementRate(Goal goal, Long currentAmount) {
        if (goal == null || goal.getTargetAmount() == null || currentAmount == null
                || goal.getTargetAmount() <= 0) {
            return ZERO_RATE;
        }
        return BigDecimal.valueOf(currentAmount)
                .divide(BigDecimal.valueOf(goal.getTargetAmount()), 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    private AssetSummary summarizeAssets(List<HomeInvestmentAsset> assets) {
        BigDecimal principal = sum(assets, HomeInvestmentAsset::getPrincipalAmount);
        BigDecimal currentValue = sum(assets, HomeInvestmentAsset::getCurrentValue);
        BigDecimal changeAmount = currentValue.subtract(principal);
        BigDecimal returnRate = calculateRate(principal, changeAmount);

        if (assets.isEmpty()) {
            return new AssetSummary(returnRate, changeAmount, List.of(), ZERO_RATE, BigDecimal.ZERO);
        }

        List<Long> assetIds = assets.stream().map(HomeInvestmentAsset::getId).toList();
        List<HomeInvestmentAssetSnapshot> snapshots = snapshotRepository
                .findAllByInvestmentAssetIdInOrderBySnapshotDateAsc(assetIds);
        Map<LocalDate, SnapshotAggregate> aggregates = aggregateByDate(snapshots);
        List<AssetReturnRateGraphDTO> graph = aggregates.entrySet().stream()
                .map(entry -> new AssetReturnRateGraphDTO(entry.getKey(),
                        calculateRate(entry.getValue().principal(), entry.getValue().changeAmount())))
                .toList();

        if (aggregates.size() < 2) {
            return new AssetSummary(returnRate, changeAmount, graph, ZERO_RATE, BigDecimal.ZERO);
        }
        SnapshotAggregate latest = new ArrayList<>(aggregates.values()).get(aggregates.size() - 1);
        SnapshotAggregate previous = new ArrayList<>(aggregates.values()).get(aggregates.size() - 2);
        BigDecimal todayChangeAmount = latest.changeAmount().subtract(previous.changeAmount());
        return new AssetSummary(returnRate, changeAmount, graph,
                calculateRate(previous.currentValue(), todayChangeAmount), todayChangeAmount);
    }

    private Map<LocalDate, SnapshotAggregate> aggregateByDate(List<HomeInvestmentAssetSnapshot> snapshots) {
        Map<LocalDate, SnapshotAggregate> aggregates = new HashMap<>();
        for (HomeInvestmentAssetSnapshot snapshot : snapshots) {
            SnapshotAggregate aggregate = aggregates.computeIfAbsent(snapshot.getSnapshotDate(),
                    ignored -> new SnapshotAggregate());
            aggregate.add(snapshot);
        }
        return aggregates.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                        Map::putAll);
    }

    private BigDecimal calculateRate(BigDecimal principal, BigDecimal changeAmount) {
        if (principal == null || principal.signum() <= 0) {
            return ZERO_RATE;
        }
        return changeAmount.divide(principal, 6, RoundingMode.HALF_UP)
                .multiply(HUNDRED)
                .setScale(RATE_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(Collection<HomeInvestmentAsset> assets,
                           java.util.function.Function<HomeInvestmentAsset, BigDecimal> getter) {
        return assets.stream()
                .map(getter)
                .filter(value -> value != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private record AssetSummary(
            BigDecimal returnRate,
            BigDecimal changeAmount,
            List<AssetReturnRateGraphDTO> graph,
            BigDecimal todayReturnRate,
            BigDecimal todayChangeAmount
    ) {
    }

    private static final class SnapshotAggregate {
        private BigDecimal principal = BigDecimal.ZERO;
        private BigDecimal changeAmount = BigDecimal.ZERO;
        private BigDecimal currentValue = BigDecimal.ZERO;

        private void add(HomeInvestmentAssetSnapshot snapshot) {
            principal = principal.add(valueOrZero(snapshot.getPrincipalAmount()));
            currentValue = currentValue.add(valueOrZero(snapshot.getCurrentValue()));
            changeAmount = currentValue.subtract(principal);
        }

        private BigDecimal principal() {
            return principal;
        }

        private BigDecimal changeAmount() {
            return changeAmount;
        }

        private BigDecimal currentValue() {
            return currentValue;
        }

        private static BigDecimal valueOrZero(BigDecimal value) {
            return value == null ? BigDecimal.ZERO : value;
        }
    }
}
