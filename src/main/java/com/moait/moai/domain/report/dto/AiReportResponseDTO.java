package com.moait.moai.domain.report.dto;

import java.time.LocalDate;
import java.util.List;

public record AiReportResponseDTO(
        MetaDTO meta,
        HeadlineDTO headline,
        ReportPersonDTO personA,
        ReportPersonDTO personB,
        PersonRiskDTO jointFund,
        AgreedProfileDTO agreedProfile,
        RecommendationDTO recommendation,
        GoalRequirementDTO goalRequirement,
        AgreementDTO agreement,
        GoalProjectionDTO goalProjection,
        RiskReasonDTO riskReason,
        List<AdjustmentOptionDTO> adjustmentOptions,
        GoalDiagnosisDTO goalDiagnosis,
        PeerComparisonDTO peerComparison,
        PlanComparisonDTO planComparison,
        StrategyComparisonDTO strategyComparison,
        AssetAllocationDTO assetAllocation,
        LossSimulationDTO lossSimulation,
        String disclaimer
) {

    public record MetaDTO(
            LocalDate reportDate,
            Long targetAmount,
            Integer investmentMonths
    ) {
    }

    public record HeadlineDTO(
            String tone,
            String title,
            String titleHighlight,
            String description,
            String summaryTitle,
            String summaryHighlight,
            String summaryDescription
    ) {
    }

    public record ReportPersonDTO(
            String name,
            Integer preferenceScore,
            Integer userLimit,
            Integer serviceLimit,
            Integer finalLimit,
            String profileType,
            String profileTypeLabel
    ) {
    }

    public record PersonRiskDTO(
            Integer preferenceScore,
            Integer userLimit,
            Integer serviceLimit,
            Integer finalLimit,
            String profileType
    ) {
    }

    public record AgreedProfileDTO(
            Integer score,
            String profileType,
            String profileTypeLabel,
            String summary
    ) {
    }

    public record RecommendationDTO(
            Integer weightedScore,
            Integer centerScore,
            Integer rangeMin,
            Integer rangeMax,
            Integer finalMax
    ) {
    }

    public record GoalRequirementDTO(
            Double requiredAnnualReturnRate,
            Integer rangeMin,
            Integer rangeMax,
            Boolean realistic,
            Integer investmentMonths,
            String calculationMethod
    ) {
    }

    public record AgreementDTO(
            String status,
            Integer recommendedRiskScore,
            String summary,
            String rationale,
            String recommendedStrategy,
            List<String> alternatives,
            List<String> cautions,
            Boolean aiGenerated
    ) {
    }

    public record SeriesPointDTO(
            Double x,
            Double y
    ) {
    }

    public record GoalProjectionDTO(
            Long targetAmount,
            Long projectedAmount,
            Long gapAmount,
            String timelineStart,
            String timelineNow,
            String timelineEnd,
            String caption,
            List<SeriesPointDTO> actualSeries,
            List<SeriesPointDTO> idealSeries,
            List<SeriesPointDTO> projectedSeries
    ) {
    }

    public record RiskReasonDTO(
            Integer preferredScore,
            Integer maxScore,
            Integer requiredMin,
            Integer requiredMax,
            String body,
            String callout
    ) {
    }

    public record AdjustmentOptionDTO(
            String type,
            String title,
            String fromLabel,
            String toLabel,
            String keeps
    ) {
    }

    public record GoalDiagnosisDTO(
            Long currentAmount,
            Long targetAmount,
            Long projectedAmount,
            Long gapAmount
    ) {
    }

    public record PeerComparisonDTO(
            Integer readinessRate,
            Long ourSavings,
            Long peerAverageSavings,
            String percentileLabel,
            String note
    ) {
    }

    public record PlanComparisonDTO(
            Long currentPlanAmount,
            Long aiPlanAmount,
            String note,
            List<SeriesPointDTO> currentSeries,
            List<SeriesPointDTO> aiSeries,
            List<SeriesPointDTO> bandLowSeries,
            List<SeriesPointDTO> bandHighSeries
    ) {
    }

    public record StrategyComparisonDTO(
            List<StrategyOptionDTO> options,
            String note
    ) {
    }

    public record StrategyOptionDTO(
            String key,
            String name,
            Double expectedReturnRate,
            Double maxDrawdownRate,
            String targetAmountLabel,
            Boolean achievable,
            String fitDescription
    ) {
    }

    public record AssetAllocationDTO(
            String strategyLabel,
            List<AssetAllocationSliceDTO> slices,
            String note
    ) {
    }

    public record AssetAllocationSliceDTO(
            String key,
            String label,
            Integer ratio
    ) {
    }

    public record LossSimulationDTO(
            Double expectedDrawdownRate,
            Double cautionThresholdRate,
            Double allowedLossRate,
            String callout
    ) {
    }
}
