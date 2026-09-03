package com.moait.moai.common.enums;

import lombok.Getter;

/**
 * 투자성향 유형. {@code RiskProfileCalculator} 가 설문 점수(0~100)를 5구간으로 분류한 결과.
 */
@Getter
public enum RiskProfileType {

    STABLE("안정형", "원금 보전을 최우선으로 하는 성향입니다."),
    STABLE_SEEKING("안정추구형", "약간의 손실은 감수하되 안정적인 수익을 추구하는 성향입니다."),
    NEUTRAL("위험중립형", "기대수익을 위해 그에 상응하는 위험을 감수할 수 있는 성향입니다."),
    ACTIVE("적극투자형", "높은 수익을 위해 상당한 손실 위험도 감내하는 성향입니다."),
    AGGRESSIVE("공격투자형", "큰 손실 가능성을 감수하고 시장 초과수익을 적극적으로 추구하는 성향입니다.");

    private final String label;
    private final String summary;

    RiskProfileType(String label, String summary) {
        this.label = label;
        this.summary = summary;
    }
}
