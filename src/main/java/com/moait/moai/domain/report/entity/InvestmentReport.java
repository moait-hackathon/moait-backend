package com.moait.moai.domain.report.entity;

import com.moait.moai.common.entity.BaseCreatedEntity;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "investment_report")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class InvestmentReport extends BaseCreatedEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "input_target_amount")
    private Long inputTargetAmount;

    @Column(name = "input_current_amount")
    private Long inputCurrentAmount;

    @Column(name = "input_target_date")
    private LocalDate inputTargetDate;

    @Column(name = "input_monthly_contribution")
    private Long inputMonthlyContribution;

    @Column(name = "summary_message", columnDefinition = "TEXT")
    private String summaryMessage;

    @Column(name = "investment_months")
    private Integer investmentMonths;

    @Column(name = "recommended_risk_score")
    private Integer recommendedRiskScore;

    @Column(name = "recommended_strategy", columnDefinition = "TEXT")
    private String recommendedStrategy;

    @Column(name = "rationale", columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "goal_adjustments", columnDefinition = "TEXT")
    private String goalAdjustments;

    @Column(name = "status", length = 40)
    private String status;

    @Column(name = "calculation_method", length = 40)
    private String calculationMethod;

    public static InvestmentReport from(Long goalId, GoalAnalysisRequestDTO input,
            InvestmentAgreementResponseDTO result) {
        InvestmentReport entity = new InvestmentReport();
        entity.goalId = goalId;
        entity.inputTargetAmount = input.targetAmount();
        entity.inputCurrentAmount = input.currentAmount();
        entity.inputTargetDate = input.targetDate();
        entity.inputMonthlyContribution = input.monthlyContribution();
        entity.summaryMessage = result.agreement().summary();
        entity.investmentMonths = result.goalRequirement().investmentMonths();
        entity.recommendedRiskScore = result.agreement().recommendedRiskScore();
        entity.recommendedStrategy = result.agreement().recommendedStrategy();
        entity.rationale = result.agreement().rationale();
        entity.goalAdjustments = String.join("\n", result.agreement().alternatives());
        entity.status = result.agreement().status();
        entity.calculationMethod = result.goalRequirement().calculationMethod();
        return entity;
    }
}
