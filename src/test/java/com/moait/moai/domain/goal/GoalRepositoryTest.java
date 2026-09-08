package com.moait.moai.domain.goal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.SurplusBand;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import com.moait.moai.domain.analysis.service.InvestmentAnalysisInputService;
import com.moait.moai.domain.analysis.service.InvestmentAnalysisInputServiceImpl;
import com.moait.moai.domain.goal.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=none")
@Import({InvestmentAnalysisInputServiceImpl.class, GoalRepositoryTest.ValidationConfig.class})
class GoalRepositoryTest {
    @Autowired
    private GoalRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private InvestmentAnalysisInputService inputService;

    @TestConfiguration(proxyBeanMethods = false)
    static class ValidationConfig {
        @Bean
        LocalValidatorFactoryBean validator() {
            return new LocalValidatorFactoryBean();
        }
    }

    @BeforeEach
    void setUp() {
        // 분석에 필요한 MoAItDB.sql 컬럼을 명시적으로 준비한다. Hibernate로 스키마를 생성하지 않는다.
        jdbc.execute("CREATE TABLE IF NOT EXISTS `user` (id BIGINT PRIMARY KEY, is_deleted BOOLEAN)");
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS couple (
                    id BIGINT PRIMARY KEY, male_id BIGINT, female_id BIGINT, status VARCHAR(30))
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS investment_account (
                    investment_account_id BIGINT PRIMARY KEY, user_id BIGINT, is_active BOOLEAN)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS goal (
                    id BIGINT PRIMARY KEY, couple_id BIGINT NOT NULL UNIQUE,
                    target_amount BIGINT, target_date DATE, current_amount BIGINT,
                    monthly_investable_amount BIGINT, emergency_fund_months VARCHAR(20),
                    monthly_surplus_band VARCHAR(20), max_allowed_loss_rate INT,
                    loss_reaction VARCHAR(20), investment_experience VARCHAR(20), status VARCHAR(30),
                    created_at TIMESTAMP, updated_at TIMESTAMP)
                """);
        jdbc.execute("""
                CREATE TABLE IF NOT EXISTS investment_asset (
                    investment_asset_id BIGINT PRIMARY KEY, investment_account_id BIGINT NOT NULL,
                    asset_type VARCHAR(30) NOT NULL, risk_level VARCHAR(30) NOT NULL,
                    current_value DECIMAL(19,2) NOT NULL, currency_code CHAR(3) NOT NULL,
                    is_active BOOLEAN NOT NULL, created_at TIMESTAMP, updated_at TIMESTAMP)
                """);
        jdbc.update("INSERT INTO `user` VALUES (101, FALSE), (102, FALSE), (103, FALSE)");
        jdbc.update("INSERT INTO couple VALUES (201, 101, 102, 'CONNECTED')");
        jdbc.update("""
                INSERT INTO goal (id, couple_id, target_amount, current_amount,
                    monthly_investable_amount, target_date, emergency_fund_months,
                    monthly_surplus_band, max_allowed_loss_rate, loss_reaction,
                    investment_experience, status)
                VALUES (301, 201, 200000000, 100000000, 3000000, '2030-01-01',
                    'M6_12', 'B20_30', 10, 'HOLD', 'ETF_ONLY', 'ACTIVE')
                """);
        jdbc.update("INSERT INTO investment_account VALUES (401, 101, TRUE), (402, 102, TRUE), (403, 103, TRUE), (404, 101, FALSE)");
        jdbc.update("""
                INSERT INTO investment_asset (investment_asset_id, investment_account_id,
                    asset_type, risk_level, current_value, currency_code, is_active)
                VALUES (501, 401, 'STOCK', 'HIGH', 1000000, 'KRW', TRUE),
                       (502, 402, 'DEPOSIT', 'VERY_LOW', 3000000, 'KRW', TRUE),
                       (503, 403, 'CRYPTO', 'VERY_HIGH', 9999999, 'KRW', TRUE),
                       (504, 404, 'CRYPTO', 'VERY_HIGH', 9999999, 'KRW', TRUE),
                       (505, 401, 'CRYPTO', 'VERY_HIGH', 9999999, 'KRW', FALSE)
                """);
    }

    @Test
    void eitherPartnerLoadsTheSameGoalAndStoredInputs() {
        var goals = repository.findConnectedGoalsByUserId(101L);
        assertThat(goals).hasSize(1);
        var goal = goals.getFirst();
        assertThat(goal.getId()).isEqualTo(301L);
        assertThat(goal.getMonthlyInvestableAmount()).isEqualTo(3000000L);
        assertThat(goal.getMonthlySurplusBand()).isEqualTo("B20_30");
        assertThat(goal.getInvestmentExperience()).isEqualTo("ETF_ONLY");
        assertThat(repository.findConnectedGoalsByUserId(102L))
                .extracting("id").containsExactly(301L);
        assertThat(repository.findConnectedGoalsByUserId(103L)).isEmpty();
    }

    @Test
    void disconnectedCoupleIsExcluded() {
        jdbc.update("UPDATE couple SET status = 'DISCONNECTED' WHERE id = 201");
        assertThat(repository.findConnectedGoalsByUserId(101L)).isEmpty();
    }

    @Test
    void deletedPartnerIsExcluded() {
        jdbc.update("UPDATE `user` SET is_deleted = TRUE WHERE id = 102");
        assertThat(repository.findConnectedGoalsByUserId(101L)).isEmpty();
    }

    @Test
    void loadsSnapshotWithStablePartnerOrderAndExcludesInactiveAndUnrelatedAssets() {
        var input = inputService.load(101L);
        assertThat(inputService.load(102L)).isEqualTo(input);
        assertThat(input.goalId()).isEqualTo(301L);
        assertThat(input.goal().monthlyContribution()).isEqualTo(3000000L);
        assertThat(input.jointFund().surplusBand()).isEqualTo(SurplusBand.B20_30);
        assertThat(input.personA()).hasSize(1);
        assertThat(input.personA().getFirst().assetType()).isEqualTo("STOCK");
        assertThat(input.personA().getFirst().currentValue()).isEqualByComparingTo("1000000");
        assertThat(input.personB()).hasSize(1);
        assertThat(input.personB().getFirst().assetType()).isEqualTo("DEPOSIT");
    }

    @Test
    void draftGoalCannotBeAnalyzed() {
        jdbc.update("UPDATE goal SET status = 'DRAFT' WHERE id = 301");
        assertThatThrownBy(() -> inputService.load(101L))
                .isInstanceOf(InvestmentAnalysisDataException.class).hasMessageContaining("온보딩");
    }

    @Test
    void missingOrInvalidFinancialInputsAreReported() {
        jdbc.update("UPDATE goal SET monthly_investable_amount = NULL, current_amount = -1 WHERE id = 301");
        assertThatThrownBy(() -> inputService.load(101L))
                .isInstanceOf(InvestmentAnalysisDataException.class)
                .hasMessageContaining("monthlyContribution").hasMessageContaining("currentAmount");
    }

    @Test
    void unknownJointBandIsNotSubstitutedWithDefaultScore() {
        jdbc.update("UPDATE goal SET monthly_surplus_band = 'UNKNOWN' WHERE id = 301");
        assertThatThrownBy(() -> inputService.load(101L))
                .isInstanceOf(InvestmentAnalysisDataException.class).hasMessageContaining("monthly_surplus_band");
    }

    @Test
    void multipleConnectedGoalsAreRejectedInsteadOfChoosingArbitrarily() {
        jdbc.update("INSERT INTO couple VALUES (202, 101, 103, 'CONNECTED')");
        jdbc.update("INSERT INTO goal (id, couple_id, status) VALUES (302, 202, 'ACTIVE')");
        assertThatThrownBy(() -> inputService.load(101L))
                .isInstanceOf(InvestmentAnalysisDataException.class).hasMessageContaining("여러 개");
    }
}
