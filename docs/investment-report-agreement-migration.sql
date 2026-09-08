-- 기존 DB에 한 번만 적용. 신규 DB의 스키마 기준은 MoAItDB.sql.
-- 기존 행은 보존하며, 과거 보고서에 없는 값은 NULL로 유지한다.
ALTER TABLE `investment_report`
    ADD COLUMN `input_monthly_contribution` BIGINT NULL COMMENT '분석 당시 월 납입액',
    ADD COLUMN `investment_months` INT NULL COMMENT '분석 당시 목표까지 개월 수',
    ADD COLUMN `recommended_risk_score` INT NULL COMMENT '추천 중단 시 NULL',
    ADD COLUMN `recommended_strategy` TEXT NULL,
    ADD COLUMN `goal_adjustments` TEXT NULL,
    ADD COLUMN `status` VARCHAR(40) NULL COMMENT '합의안 적합성 판정',
    ADD COLUMN `calculation_method` VARCHAR(40) NULL COMMENT '위험수준 계산 방식';
