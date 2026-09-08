package com.moait.moai.domain.analysis.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.moait.moai.domain.analysis.dto.InvestmentAnalysisInputDTO.AssetPosition;
import com.moait.moai.domain.analysis.exception.InvestmentAnalysisDataException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PortfolioRiskCalculatorTest {
    private final PortfolioRiskCalculator calculator = new PortfolioRiskCalculator();

    @Test
    void weightsByCurrentValueAndRoundsOnlyFinalScore() {
        var result = calculator.calculate(List.of(
                asset("STOCK", "HIGH", "100.00", "KRW"),
                asset("DEPOSIT", "VERY_LOW", "300.00", "KRW")), "A");
        assertThat(result.preferenceScore()).isEqualTo(19); // 75 * 100 / 400 = 18.75
        assertThat(result.userLimit()).isNull();
        assertThat(result.serviceLimit()).isNull();
        assertThat(result.finalLimit()).isNull();
        assertThat(result.profileType()).isEqualTo("STABLE");
    }

    @ParameterizedTest
    @CsvSource({"VERY_LOW,0", "LOW,25", "MEDIUM,50", "HIGH,75", "VERY_HIGH,100"})
    void classifiedRiskTakesPrecedenceOverType(String risk, int expected) {
        assertThat(calculator.calculate(List.of(asset("ETF", risk, "10", "KRW")), "A")
                .preferenceScore()).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"CASH,0", "DEPOSIT,0", "SAVINGS,0", "BOND,25", "STOCK,75", "CRYPTO,100"})
    void usesDocumentedTypeDefaultOnlyWhenUnclassified(String type, int expected) {
        assertThat(calculator.calculate(List.of(asset(type, "UNCLASSIFIED", "10", "KRW")), "A")
                .preferenceScore()).isEqualTo(expected);
    }

    @Test
    void addingDepositsReducesScoreWhileZeroPositionsHaveNoEffect() {
        var positions = List.of(asset("STOCK", "HIGH", "100", "KRW"),
                asset("SAVINGS", "UNCLASSIFIED", "100", "KRW"),
                asset("OTHER", "UNCLASSIFIED", "0", "USD"));
        assertThat(calculator.calculate(positions, "A").preferenceScore()).isEqualTo(38);
    }

    @ParameterizedTest
    @CsvSource({"ETF,UNCLASSIFIED,10,KRW", "OTHER,UNCLASSIFIED,10,KRW",
            "STOCK,HIGH,10,USD", "DEPOSIT,VERY_LOW,-1,KRW", "STOCK,UNKNOWN,10,KRW"})
    void invalidOrUnsupportedPositionsAreNotSilentlyOmitted(String type, String risk, String value, String currency) {
        assertThatThrownBy(() -> calculator.calculate(List.of(asset(type, risk, value, currency)), "B"))
                .isInstanceOf(InvestmentAnalysisDataException.class).hasMessageContaining("개인 B");
    }

    @Test
    void missingHoldingsAreNotTreatedAsConservativePreference() {
        assertThatThrownBy(() -> calculator.calculate(List.of(), "A"))
                .isInstanceOf(InvestmentAnalysisDataException.class);
        assertThatThrownBy(() -> calculator.calculate(List.of(asset("STOCK", "HIGH", "0", "KRW")), "B"))
                .isInstanceOf(InvestmentAnalysisDataException.class);
    }

    private AssetPosition asset(String type, String risk, String value, String currency) {
        return new AssetPosition(type, risk, new BigDecimal(value), currency);
    }
}
