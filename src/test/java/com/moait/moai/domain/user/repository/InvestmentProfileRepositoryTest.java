package com.moait.moai.domain.user.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.common.enums.HoldingAssetType;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.InvestmentHorizon;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.user.entity.HoldingAsset;
import com.moait.moai.domain.user.entity.InvestmentProfile;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class InvestmentProfileRepositoryTest {

    @Autowired
    private InvestmentProfileRepository repository;

    @Autowired
    private TestEntityManager em;

    private InvestmentProfile sample(Long userId) {
        return InvestmentProfile.create(
                userId,
                InvestmentExperience.ETF_ONLY,
                LossReaction.HOLD,
                15,
                InvestmentHorizon.Y3_5,
                List.of(new HoldingAsset(HoldingAssetType.DEPOSIT, 10_000_000L),
                        new HoldingAsset(HoldingAssetType.ETF, 5_000_000L)),
                500_000L,
                true,
                RiskProfileType.NEUTRAL,
                58);
    }

    @Test
    @DisplayName("holding_assets(JSON) 를 List<HoldingAsset> 로 저장/조회한다")
    void jsonRoundTrip() {
        InvestmentProfile saved = repository.save(sample(1L));
        em.flush();
        em.clear();

        InvestmentProfile found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getHoldingAssets())
                .containsExactly(
                        new HoldingAsset(HoldingAssetType.DEPOSIT, 10_000_000L),
                        new HoldingAsset(HoldingAssetType.ETF, 5_000_000L));
        assertThat(found.getInvestmentExperience()).isEqualTo(InvestmentExperience.ETF_ONLY);
        assertThat(found.getRiskProfileType()).isEqualTo(RiskProfileType.NEUTRAL);
        assertThat(found.getIsLatest()).isTrue();
    }

    @Test
    @DisplayName("markAllStale 은 해당 사용자의 최신 프로필만 is_latest=false 로 바꾼다")
    void markAllStale() {
        repository.save(sample(1L));
        repository.save(sample(1L));
        InvestmentProfile other = repository.save(sample(2L));
        em.flush();
        em.clear();

        repository.markAllStale(1L);
        em.clear();

        assertThat(repository.existsByUserIdAndIsLatestTrue(1L)).isFalse();
        assertThat(repository.findById(other.getId()).orElseThrow().getIsLatest()).isTrue();
    }
}
