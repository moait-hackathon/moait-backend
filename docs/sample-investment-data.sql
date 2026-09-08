-- MoAIt 투자 분석 로컬 테스트 데이터
-- 선행 조건: MoAItDB.sql 실행 후 moait 스키마가 생성되어 있어야 합니다.
-- 테스트 전용으로 990001 이상의 고정 ID를 사용합니다.

USE `moait`;

START TRANSACTION;

INSERT INTO `user` (
    `id`, `phone`, `password`, `name`, `role`, `gender`, `provider`,
    `asset`, `income`, `is_deleted`
) VALUES
    (990001, '01099000001', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '테스트A', 'USER', 'MALE', 'LOCAL', 180000000, 6000000, FALSE),
    (990002, '01099000002', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
     '테스트B', 'USER', 'FEMALE', 'LOCAL', 120000000, 4500000, FALSE)
ON DUPLICATE KEY UPDATE
    `asset` = VALUES(`asset`),
    `income` = VALUES(`income`),
    `is_deleted` = FALSE;

INSERT INTO `couple` (`id`, `male_id`, `female_id`, `status`, `connected_at`)
VALUES (990001, 990001, 990002, 'CONNECTED', NOW())
ON DUPLICATE KEY UPDATE
    `status` = 'CONNECTED',
    `connected_at` = VALUES(`connected_at`);

INSERT INTO `goal` (
    `id`, `couple_id`, `target_amount`, `current_amount`,
    `current_amount_updated_at`, `target_date`, `max_allowed_loss_rate`,
    `monthly_investable_amount`, `emergency_fund_months`, `monthly_surplus_band`,
    `loss_reaction`, `investment_experience`, `investment_period_months`,
    `risk_profile_score`, `joint_risk_profile_type`, `status`
) VALUES (
    990001, 990001, 200000000, 100000000,
    NOW(), DATE_ADD(CURDATE(), INTERVAL 2 YEAR), 10,
    3000000, 'M6_12', 'B20_30', 'HOLD', 'ETF_ONLY', 24,
    61, 'ACTIVE', 'ACTIVE'
)
ON DUPLICATE KEY UPDATE
    `target_amount` = VALUES(`target_amount`),
    `current_amount` = VALUES(`current_amount`),
    `current_amount_updated_at` = VALUES(`current_amount_updated_at`),
    `target_date` = VALUES(`target_date`),
    `max_allowed_loss_rate` = VALUES(`max_allowed_loss_rate`),
    `monthly_investable_amount` = VALUES(`monthly_investable_amount`),
    `emergency_fund_months` = VALUES(`emergency_fund_months`),
    `monthly_surplus_band` = VALUES(`monthly_surplus_band`),
    `loss_reaction` = VALUES(`loss_reaction`),
    `investment_experience` = VALUES(`investment_experience`),
    `investment_period_months` = VALUES(`investment_period_months`),
    `risk_profile_score` = VALUES(`risk_profile_score`),
    `joint_risk_profile_type` = VALUES(`joint_risk_profile_type`),
    `status` = 'ACTIVE';

INSERT INTO `investment_account` (
    `investment_account_id`, `user_id`, `institution_code`, `institution_name`,
    `account_name`, `account_type`, `account_number_masked`, `source_type`, `is_active`
) VALUES
    (990001, 990001, 'MOAI_TEST_SEC', '테스트증권', 'A 투자계좌',
     'SECURITIES', '123-****-0001', 'MANUAL', TRUE),
    (990002, 990002, 'MOAI_TEST_BANK', '테스트은행', 'B 저축계좌',
     'BANK', '456-****-0002', 'MANUAL', TRUE)
ON DUPLICATE KEY UPDATE
    `institution_name` = VALUES(`institution_name`),
    `account_name` = VALUES(`account_name`),
    `is_active` = TRUE;

INSERT INTO `investment_asset` (
    `investment_asset_id`, `investment_account_id`, `asset_type`, `risk_level`,
    `market_type`, `product_code`, `product_name`, `principal_amount`,
    `current_value`, `evaluation_profit_loss`, `return_rate`, `source_type`, `currency_code`,
    `valuation_base_date`, `is_active`
) VALUES
    (990001, 990001, 'ETF', 'HIGH', 'DOMESTIC', 'TEST-ETF-001',
     '테스트 주식형 ETF', 30000000, 33000000, 3000000, 10.0000,
     'MANUAL', 'KRW', CURDATE(), TRUE),
    (990002, 990001, 'STOCK', 'VERY_HIGH', 'FOREIGN', 'TEST-STOCK-001',
     '테스트 해외주식', 20000000, 18000000, -2000000, -10.0000,
     'MANUAL', 'KRW', CURDATE(), TRUE),
    (990003, 990002, 'DEPOSIT', 'VERY_LOW', 'NOT_APPLICABLE', 'TEST-DEP-001',
     '테스트 정기예금', 30000000, 30500000, 500000, 1.6667,
     'MANUAL', 'KRW', CURDATE(), TRUE),
    (990004, 990002, 'ETF', 'MEDIUM', 'DOMESTIC', 'TEST-ETF-002',
     '테스트 채권형 ETF', 10000000, 10200000, 200000, 2.0000,
     'MANUAL', 'KRW', CURDATE(), TRUE)
ON DUPLICATE KEY UPDATE
    `asset_type` = VALUES(`asset_type`),
    `risk_level` = VALUES(`risk_level`),
    `currency_code` = VALUES(`currency_code`),
    `principal_amount` = VALUES(`principal_amount`),
    `current_value` = VALUES(`current_value`),
    `evaluation_profit_loss` = VALUES(`evaluation_profit_loss`),
    `return_rate` = VALUES(`return_rate`),
    `valuation_base_date` = VALUES(`valuation_base_date`),
    `is_active` = TRUE;

COMMIT;

-- 입력 확인
SELECT `id`, `name`, `asset`, `income` FROM `user` WHERE `id` IN (990001, 990002);
SELECT * FROM `couple` WHERE `id` = 990001;
SELECT * FROM `goal` WHERE `id` = 990001;
SELECT * FROM `investment_account` WHERE `user_id` IN (990001, 990002);
SELECT ia.`user_id`, ast.*
FROM `investment_asset` ast
JOIN `investment_account` ia
  ON ia.`investment_account_id` = ast.`investment_account_id`
WHERE ia.`user_id` IN (990001, 990002);
