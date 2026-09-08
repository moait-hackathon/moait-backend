package com.moait.moai.domain.goal.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.common.enums.EmergencyFundMonths;
import com.moait.moai.common.enums.Gender;
import com.moait.moai.common.enums.GoalStatus;
import com.moait.moai.common.enums.InvestmentExperience;
import com.moait.moai.common.enums.LossReaction;
import com.moait.moai.common.enums.MonthlySurplusBand;
import com.moait.moai.common.enums.RiskProfileType;
import com.moait.moai.domain.couple.entity.Couple;
import com.moait.moai.domain.goal.entity.Goal;
import com.moait.moai.domain.user.entity.User;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
class GoalRepositoryTest {

    @Autowired
    private GoalRepository repository;

    @Autowired
    private TestEntityManager em;

    private Long persistCouple() {
        User male = User.createLocal("남", null, "pw", Gender.MALE);
        User female = User.createLocal("여", null, "pw", Gender.FEMALE);
        em.persist(male);
        em.persist(female);
        Couple couple = Couple.createWait(male.getId(), female.getId());
        couple.connect();
        em.persist(couple);
        return couple.getId();
    }

    @Test
    @DisplayName("findByCoupleId / existsByCoupleId + 열거형·감사필드 매핑")
    void persistAndFind() {
        Long coupleId = persistCouple();
        Goal goal = Goal.create(coupleId, 100_000_000L, LocalDate.now().plusYears(5), 60,
                20_000_000L, 500_000L, EmergencyFundMonths.M3_6, MonthlySurplusBand.B20_30,
                10, LossReaction.HOLD, InvestmentExperience.ETF_ONLY, 62, RiskProfileType.ACTIVE);
        em.persist(goal);
        em.flush();
        em.clear();

        assertThat(repository.existsByCoupleId(coupleId)).isTrue();
        Goal found = repository.findByCoupleId(coupleId).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(GoalStatus.ACTIVE);
        assertThat(found.getJointRiskProfileType()).isEqualTo(RiskProfileType.ACTIVE);
        assertThat(found.getEmergencyFundMonths()).isEqualTo(EmergencyFundMonths.M3_6);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
