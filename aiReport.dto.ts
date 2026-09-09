// AI 투자 합의안 리포트 API DTO.
//
// personA/personB/jointFund/recommendation/goalRequirement/agreement 는
// 백엔드가 제공한 응답 예시(`POST /ai-report` 성격) 스펙을 그대로 옮긴 것이다.
// 그 밖의 필드(meta/headline/goalProjection/... )는 화면 구성을 위해 프론트에서
// 임시로 정의한 형태이며, 백엔드 스펙 확정 시 맞춰 조정한다.
// (백엔드 미구현 상태 → server/mocks/*.json 목업으로 화면을 그린다.)

// 합의 상태. UNSUITABLE 이면 "목표 조정 필요" 화면, 그 외에는 "적정" 화면.
export type AgreementStatus = 'SUITABLE' | 'LOWER_RISK_SUFFICIENT' | 'UNSUITABLE';

// 위험성향 유형. jointRiskProfileType 와 별개로 리포트 전용 축(균형형/성장추구형 등).
export type RiskProfileType =
  'STABLE' | 'STABLE_SEEKING' | 'BALANCED' | 'GROWTH_SEEKING' | 'ACTIVE' | 'AGGRESSIVE';

export type HeadlineTone = 'POSITIVE' | 'WARNING';

export type AdjustmentType = 'MONTHLY_CONTRIBUTION' | 'TARGET_AMOUNT' | 'TARGET_DATE';

export type StrategyKey = 'DEFENSIVE' | 'GROWTH';

// 그래프 좌표. x·y 모두 0~1 로 정규화한 값. (x = 시간축, y = 금액축)
export interface SeriesPointDto {
  x: number;
  y: number;
}

// --- 백엔드 확정 스펙 ---------------------------------------------------------

export interface PersonRiskDto {
  preferenceScore: number;
  userLimit: number;
  serviceLimit: number;
  finalLimit: number;
  profileType: RiskProfileType;
}

export interface RecommendationDto {
  weightedScore: number;
  centerScore: number;
  rangeMin: number;
  rangeMax: number;
  finalMax: number;
}

export interface GoalRequirementDto {
  requiredAnnualReturnRate: number;
  rangeMin: number;
  rangeMax: number;
  realistic: boolean;
  investmentMonths: number;
  calculationMethod: string;
}

export interface AgreementDto {
  status: AgreementStatus;
  recommendedRiskScore: number | null;
  summary: string;
  rationale: string;
  recommendedStrategy: string | null;
  alternatives: string[];
  cautions: string[];
  aiGenerated: boolean;
}

// --- 화면용 임시 필드 (백엔드 확정 시 조정) ---------------------------------

export interface ReportMetaDto {
  reportDate: string; // ISO date, 예: "2026-09-08"
  targetAmount: number;
  investmentMonths: number;
}

// 리포트에 이름/한글 유형명을 얹은 사람 정보.
export interface ReportPersonDto extends PersonRiskDto {
  name: string;
  profileTypeLabel: string;
}

export interface AgreedProfileDto {
  score: number;
  profileType: RiskProfileType;
  profileTypeLabel: string;
  // "부부 위험성향과 합의 결과" 하단 설명 문구.
  summary: string;
}

export interface HeadlineDto {
  tone: HeadlineTone;
  title: string;
  titleHighlight: string;
  description: string;
  summaryTitle: string;
  summaryHighlight: string;
  summaryDescription: string;
}

// 목표 달성 예상 그래프. *Series 는 0~1 로 정규화한 y 좌표 배열.
export interface GoalProjectionDto {
  targetAmount: number;
  projectedAmount: number;
  gapAmount: number; // 양수 = 초과, 음수 = 부족
  timelineStart: string;
  timelineNow: string;
  timelineEnd: string;
  caption: string;
  actualSeries: SeriesPointDto[];
  idealSeries: SeriesPointDto[];
  projectedSeries: SeriesPointDto[];
}

export interface RiskReasonDto {
  body: string;
  preferredScore: number;
  maxScore: number;
  requiredMin: number;
  requiredMax: number;
  callout: string;
}

export interface AdjustmentOptionDto {
  type: AdjustmentType;
  title: string;
  fromLabel: string;
  toLabel: string;
  keeps: string;
}

export interface GoalDiagnosisDto {
  currentAmount: number;
  targetAmount: number;
  projectedAmount: number;
  gapAmount: number; // 양수 = 예상 초과액, 음수 = 예상 부족액
}

export interface PeerComparisonDto {
  readinessRate: number;
  ourSavings: number;
  peerAverageSavings: number;
  percentileLabel: string;
  note: string;
}

export interface PlanComparisonDto {
  currentPlanAmount: number;
  aiPlanAmount: number;
  note: string;
  currentSeries: SeriesPointDto[];
  aiSeries: SeriesPointDto[];
  bandLowSeries: SeriesPointDto[];
  bandHighSeries: SeriesPointDto[];
}

export interface StrategyOptionDto {
  key: StrategyKey;
  name: string;
  expectedReturnRate: number;
  maxDrawdownRate: number; // 음수
  targetAmountLabel: string;
  achievable: boolean;
  fitDescription: string;
}

export interface StrategyComparisonDto {
  options: StrategyOptionDto[];
  note: string;
}

export interface AssetAllocationSliceDto {
  key: string;
  label: string;
  ratio: number; // %
}

export interface AssetAllocationDto {
  strategyLabel: string;
  slices: AssetAllocationSliceDto[];
  note: string;
}

// 손실 위험 게이지. rate 값은 음수(%) 낙폭.
export interface LossSimulationDto {
  expectedDrawdownRate: number;
  cautionThresholdRate: number;
  allowedLossRate: number;
  callout: string;
}

export interface AiReportDataDto {
  meta: ReportMetaDto;
  headline: HeadlineDto;
  personA: ReportPersonDto;
  personB: ReportPersonDto;
  jointFund: PersonRiskDto;
  agreedProfile: AgreedProfileDto;
  recommendation: RecommendationDto;
  goalRequirement: GoalRequirementDto;
  agreement: AgreementDto;
  goalProjection: GoalProjectionDto;
  riskReason: RiskReasonDto;
  adjustmentOptions: AdjustmentOptionDto[]; // status !== UNSUITABLE 이면 []
  goalDiagnosis: GoalDiagnosisDto;
  peerComparison: PeerComparisonDto;
  planComparison: PlanComparisonDto;
  strategyComparison: StrategyComparisonDto | null; // UNSUITABLE 이면 null
  assetAllocation: AssetAllocationDto;
  lossSimulation: LossSimulationDto;
  disclaimer: string;
}

// GET /ai-report/me 응답 래퍼.
export interface AiReportResponseDto {
  data: AiReportDataDto;
  message: string;
  success: boolean;
}
