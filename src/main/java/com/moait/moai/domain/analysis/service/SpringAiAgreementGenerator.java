package com.moait.moai.domain.analysis.service;

import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Agreement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.GoalRequirement;
import com.moait.moai.domain.analysis.dto.InvestmentAgreementResponseDTO.Recommendation;
import com.moait.moai.domain.analysis.dto.GoalAnalysisRequestDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpringAiAgreementGenerator implements AgreementGenerator {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final GoalAdjustmentCalculator adjustmentCalculator;

    @Override
    public Agreement generate(Recommendation r, GoalRequirement g,
            GoalAnalysisRequestDTO goal, String status) {
        List<String> adjustments = alternatives(r, g, goal, status);
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return fallback(r, g, status, adjustments);
        }
        try {
            AiAgreement result = builder.build().prompt()
                    .system("""
                            당신은 예비부부 공동자금의 투자 합의를 중재하는 금융 교육용 AI다.
                            제공된 계산값을 변경하거나 새로운 수익률을 만들지 않는다.
                            G는 임시 수익률 구간 규칙이며 시뮬레이션이나 달성 확률이 아니다.
                            새로운 전략을 선택하지 말고 제공된 판정만 설명한다.
                            R과 G 및 모든 범위 숫자는 0~100의 위험점수다. 위험점수를 금액,
                            수익률, 납입 증가율 또는 투자기간으로 해석하거나 표현하지 않는다.
                            목표금액, 현재금액, 월 납입액, 목표일은 별도로 제공된 실제 값만 사용한다.
                            제공된 목표 조정안(adjustments)에 구체적인 금액, 증액/감액 금액, 개월 수, 단축/연장 목표일 숫자가 포함되어 있다.
                            요약(summary)과 근거(rationale) 작성 시 "목표금액, 월 납입액, 목표일을 조정해야 합니다"와 같은 일반적이고 추상적인 문장으로 뭉뚱그리지 마라.
                            대신 제공된 조정안(adjustments)의 구체적인 숫자(예: 월 X원 증액/감액, Y개월 연장/단축, 목표금액 Z원으로 조정)를 명확하게 포함하여 구체적으로 요약하고 근거를 설명하라.
                            UNSUITABLE은 현재 계획의 투자전략 추천 중단을 뜻한다.
                            이 경우에도 제공된 목표 조정안의 구체적 수치를 활용하여 설명한다.
                            위험 상향을 먼저 권하지 말고 납입액, 기간, 목표금액 조정을 우선한다.
                            수익을 보장하지 말고 간결하고 중립적인 한국어로 답한다.
                            """)
                    .user(u -> u.text("""
                            상태={status}, 최종 R={r}, 목표 요구 위험점수={gMin}, 표시용 범위={rMin}~{rMax}, 최종상한={finalMax},
                            목표 요구범위={gMin}~{gMax}, 필요 연수익률={returnRate}%, 현실성={realistic}.
                            실제 목표 데이터: 목표금액={targetAmount}원, 현재금액={currentAmount}원,
                            월 납입액={monthlyContribution}원,
                            목표일={targetDate}.
                            서버가 계산한 목표 조정안: {adjustments}
                            두 사람이 확인할 합의안과 근거, 주의사항을 작성하라.
                            """)
                            .param("adjustments", String.join("\n", adjustments))
                            .param("status", status)
                            .param("r", r.centerScore())
                            .param("rMin", r.rangeMin()).param("rMax", r.rangeMax())
                            .param("finalMax", r.finalMax()).param("gMin", g.rangeMin())
                            .param("gMax", g.rangeMax()).param("returnRate", g.requiredAnnualReturnRate() == null ? "산출 불가" : g.requiredAnnualReturnRate())
                            .param("realistic", g.realistic())
                            .param("targetAmount", goal.targetAmount())
                            .param("currentAmount", goal.currentAmount())
                            .param("monthlyContribution", goal.monthlyContribution())
                            .param("targetDate", goal.targetDate()))
                    .call()
                    .entity(AiAgreement.class, spec -> spec.useProviderStructuredOutput());
            if (result == null || result.summary() == null || result.summary().isBlank()
                    || result.rationale() == null || result.rationale().isBlank()
                    || result.cautions() == null) {
                return fallback(r, g, status, adjustments);
            }
            return new Agreement(status, recommendedScore(g, status), result.summary(), result.rationale(),
                    strategy(g, status), adjustments, result.cautions(), true);
        } catch (RuntimeException e) {
            log.warn("AI agreement generation failed; deterministic fallback used: {}",
                    e.getClass().getSimpleName());
            return fallback(r, g, status, adjustments);
        }
    }

    private Agreement fallback(Recommendation r, GoalRequirement g, String status, List<String> adjustments) {
        String summary = switch (status) {
            case "SUITABLE" -> "합의한 위험범위 안에서 목표를 추진할 수 있습니다.";
            case "LOWER_RISK_SUFFICIENT" -> "현재 목표는 더 보수적인 전략으로도 추진할 수 있습니다.";
            case "GOAL_INCREASE_POSSIBLE" -> "보수적인 전략을 유지하면서 목표 상향을 검토할 여지가 있습니다.";
            case "CONDITIONAL" -> "선호범위보다 높은 위험이 필요하므로 두 사람의 추가 확인이 필요합니다.";
            default -> "현재 조건에서는 허용한 위험범위 안에서 목표 달성이 어렵습니다.";
        };
        return new Agreement(status, recommendedScore(g, status), summary,
                "규칙 기반 G-R 및 최종 상한 비교입니다. G는 임시 수익률 구간이며 달성 확률은 계산하지 않았습니다.",
                strategy(g, status), adjustments,
                List.of("예상수익률은 보장되지 않으며 원금 손실이 발생할 수 있습니다."), false);
    }

    private Integer recommendedScore(GoalRequirement g, String status) {
        return "UNSUITABLE".equals(status) ? null : g.rangeMin();
    }

    private String strategy(GoalRequirement g, String status) {
        return "UNSUITABLE".equals(status) ? null : "위험점수 " + g.rangeMin() + " 수준의 최소 위험 전략 (임시 구간 기준)";
    }

    private List<String> alternatives(Recommendation r, GoalRequirement g, GoalAnalysisRequestDTO goal, String status) {
        return adjustmentCalculator.calculate(r, g, goal);
    }

    private record AiAgreement(String summary, String rationale, List<String> cautions) { }
}
