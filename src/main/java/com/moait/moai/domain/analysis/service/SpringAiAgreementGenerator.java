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

    private static final List<String> DEFAULT_ALTERNATIVES = List.of(
            "월 납입액 증가", "목표기간 연장", "목표금액 조정",
            "필수목표와 추가목표 분리", "위험수준 재협의");

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    @Override
    public Agreement generate(Recommendation r, GoalRequirement g,
            GoalAnalysisRequestDTO goal, String status) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return fallback(r, g, status);
        }
        try {
            AiAgreement result = builder.build().prompt()
                    .system("""
                            당신은 예비부부 공동자금의 투자 합의를 중재하는 금융 교육용 AI다.
                            제공된 계산값을 변경하거나 새로운 수익률을 만들지 않는다.
                            R과 G 및 모든 범위 숫자는 0~100의 위험점수다. 위험점수를 금액,
                            수익률, 납입 증가율 또는 투자기간으로 해석하거나 표현하지 않는다.
                            목표금액, 현재금액, 월 납입액, 목표일은 별도로 제공된 실제 값만 사용한다.
                            계산 근거가 제공되지 않은 구체적인 조정 금액, 증가율, 연장 기간을
                            임의로 만들지 말고 정성적인 대안으로 제시한다.
                            위험 상향을 먼저 권하지 말고 납입액, 기간, 목표금액 조정을 우선한다.
                            수익을 보장하지 말고 간결하고 중립적인 한국어로 답한다.
                            """)
                    .user(u -> u.text("""
                            상태={status}, 권장범위={rMin}~{rMax}, 최종상한={finalMax},
                            목표 요구범위={gMin}~{gMax}, 필요 연수익률={returnRate}%, 현실성={realistic}.
                            실제 목표 데이터: 목표금액={targetAmount}원, 현재금액={currentAmount}원,
                            월 납입액={monthlyContribution}원, 추가입금={additionalDeposit}원,
                            예정인출={plannedWithdrawal}원, 예상 세금·수수료={fees}원,
                            목표일={targetDate}.
                            두 사람이 확인할 합의안과 근거, 대안, 주의사항을 작성하라.
                            """)
                            .param("status", status)
                            .param("rMin", r.rangeMin()).param("rMax", r.rangeMax())
                            .param("finalMax", r.finalMax()).param("gMin", g.rangeMin())
                            .param("gMax", g.rangeMax()).param("returnRate", g.requiredAnnualReturnRate())
                            .param("realistic", g.realistic())
                            .param("targetAmount", goal.targetAmount())
                            .param("currentAmount", goal.currentAmount())
                            .param("monthlyContribution", goal.monthlyContribution())
                            .param("additionalDeposit", goal.additionalDeposit())
                            .param("plannedWithdrawal", goal.plannedWithdrawal())
                            .param("fees", goal.estimatedFeesAndTaxes())
                            .param("targetDate", goal.targetDate()))
                    .call()
                    .entity(AiAgreement.class, spec -> spec.useProviderStructuredOutput());
            return new Agreement(status, recommendedScore(r, g), result.summary(), result.rationale(),
                    result.alternatives(), result.cautions(), true);
        } catch (RuntimeException e) {
            log.warn("AI agreement generation failed; deterministic fallback used: {}",
                    e.getClass().getSimpleName());
            return fallback(r, g, status);
        }
    }

    private Agreement fallback(Recommendation r, GoalRequirement g, String status) {
        String summary = switch (status) {
            case "SUITABLE" -> "합의한 위험범위 안에서 목표를 추진할 수 있습니다.";
            case "LOWER_RISK_SUFFICIENT" -> "현재 목표는 더 보수적인 전략으로도 추진할 수 있습니다.";
            case "CONDITIONAL" -> "선호범위보다 높은 위험이 필요하므로 두 사람의 추가 확인이 필요합니다.";
            default -> "현재 조건에서는 허용한 위험범위 안에서 목표 달성이 어렵습니다.";
        };
        return new Agreement(status, recommendedScore(r, g), summary,
                "목표 요구범위와 최종 허용상한을 비교한 결과입니다.",
                "UNSUITABLE".equals(status) ? DEFAULT_ALTERNATIVES : List.of(),
                List.of("예상수익률은 보장되지 않으며 원금 손실이 발생할 수 있습니다."), false);
    }

    private int recommendedScore(Recommendation r, GoalRequirement g) {
        return Math.min(r.finalMax(), Math.max(g.rangeMin(), Math.min(r.rangeMax(), r.rangeMin())));
    }

    private record AiAgreement(String summary, String rationale,
                               List<String> alternatives, List<String> cautions) { }
}
