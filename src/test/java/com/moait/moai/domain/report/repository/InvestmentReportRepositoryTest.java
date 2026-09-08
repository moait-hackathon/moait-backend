package com.moait.moai.domain.report.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Agreement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.report.entity.InvestmentReport;
import jakarta.persistence.EntityManager;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:report-repository;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=none"
})
@Transactional
class InvestmentReportRepositoryTest {
    private final InvestmentReportRepository repository;
    private final JdbcTemplate jdbc;
    private final EntityManager entityManager;

    @Autowired
    InvestmentReportRepositoryTest(InvestmentReportRepository repository, JdbcTemplate jdbc,
            EntityManager entityManager) {
        this.repository = repository;
        this.jdbc = jdbc;
        this.entityManager = entityManager;
    }

    @BeforeEach
    void prepareSchemaFromSourceOfTruth() throws Exception {
        String source = Files.readString(Path.of("MoAItDB.sql"));
        var tables = Pattern.compile("CREATE TABLE `(user|couple|goal|investment_report)` \\(.*?"
                + "\\) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;", Pattern.DOTALL).matcher(source);
        while (tables.find()) {
            jdbc.execute(tables.group().replace("CREATE TABLE", "CREATE TABLE IF NOT EXISTS")
                    .replace(" ENGINE = InnoDB DEFAULT CHARSET = utf8mb4", ""));
        }
        jdbc.update("INSERT INTO `user` (id, password, name) VALUES (1, 'test', 'A'), (2, 'test', 'B')");
        jdbc.update("INSERT INTO couple (id, male_id, female_id, status) VALUES (10, 1, 2, 'CONNECTED')");
        jdbc.update("INSERT INTO goal (id, couple_id) VALUES (100, 10)");
    }

    @Test
    void onlyConnectedCoupleMembersCanAccessExistingGoal() {
        assertThat(repository.countAccessibleGoal(100L, 1L)).isEqualTo(1);
        assertThat(repository.countAccessibleGoal(100L, 2L)).isEqualTo(1);
        assertThat(repository.countAccessibleGoal(100L, 3L)).isZero();
        assertThat(repository.countAccessibleGoal(999L, 1L)).isZero();
        jdbc.update("UPDATE couple SET status = 'DISCONNECTED' WHERE id = 10");
        assertThat(repository.countAccessibleGoal(100L, 1L)).isZero();
    }

    @Test
    void persistsGoalLinkedReportAndKeepsLegacyRowsReadable() {
        jdbc.update("INSERT INTO investment_report (id, goal_id, rationale) VALUES (999, 100, 'legacy')");
        var input = new GoalAnalysisRequestDTO(200L, 100L, 10L, LocalDate.of(2030, 1, 1));
        var response = new InvestmentAgreementResponseDTO(null, null, null, null,
                new GoalRequirement(null, 100, 100, false, 24, "LEGACY_RETURN_BANDS"),
                new Agreement("UNSUITABLE", null, "summary", "rationale", null,
                        List.of("increase contribution", "extend period"), List.of(), false));
        var saved = repository.saveAndFlush(InvestmentReport.from(100L, input, response));
        entityManager.clear();
        var loaded = repository.findById(saved.getId()).orElseThrow();
        assertThat(loaded.getGoalId()).isEqualTo(100L);
        assertThat(loaded.getInputMonthlyContribution()).isEqualTo(10L);
        assertThat(loaded.getInputTargetDate()).isEqualTo(input.targetDate());
        assertThat(loaded.getRecommendedRiskScore()).isNull();
        assertThat(loaded.getRecommendedStrategy()).isNull();
        assertThat(loaded.getRationale()).isEqualTo("rationale");
        assertThat(loaded.getGoalAdjustments()).isEqualTo("increase contribution\nextend period");
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(repository.findById(999L).orElseThrow().getCalculationMethod()).isNull();
    }
}
