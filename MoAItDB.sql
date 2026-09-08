-- =====================================================================
--  MoAIt - Database Schema
--  MySQL 8.x / InnoDB / utf8mb4
--
--  스키마의 소스 오브 트루스. ERDCloud export를 기반으로 제약·인덱스 보정,
--  자산 도메인 source_type 문법 오류 수정 완료.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `moait`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;
USE `moait`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ---------------------------------------------------------------------
--  user
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT,
    `phone`        VARCHAR(20)  NULL COMMENT '휴대폰번호 (숫자만)',
    `password`     VARCHAR(255) NOT NULL,
    `name`         VARCHAR(50)  NOT NULL,
    `role`         VARCHAR(10)  NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
    `gender`       VARCHAR(10)  NULL COMMENT 'MALE / FEMALE',
    `provider`     VARCHAR(10)  NULL DEFAULT 'LOCAL' COMMENT 'LOCAL / KAKAO / NAVER',
    `provider_id`  VARCHAR(255) NULL COMMENT '소셜 로그인 식별자',
    `connected_id` VARCHAR(255) NULL COMMENT 'CODEF 커넥티드 아이디',
    `asset`        BIGINT       NULL COMMENT '총 자산(원, 자가입력)',
    `income`       BIGINT       NULL COMMENT '연소득(원, 자가입력)',
    `is_deleted`   BOOLEAN      NULL DEFAULT FALSE COMMENT '회원 탈퇴 여부',
    `created_at`   DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_phone` (`phone`),
    KEY `idx_user_provider` (`provider`, `provider_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  invitation  (커플 초대 코드 - 마스터 row + 요청자별 복사 row)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `invitation`;
CREATE TABLE `invitation` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT,
    `inviter_id`  BIGINT      NOT NULL COMMENT '코드 소유자',
    `invitee_id`  BIGINT      NULL COMMENT '코드 입력한 사용자 (마스터 row는 NULL)',
    `invite_code` VARCHAR(30) NOT NULL COMMENT '6자리 영숫자, 만료 없음',
    `status`      VARCHAR(30) NOT NULL DEFAULT 'CREATED' COMMENT 'CREATED / REQUESTED / ACCEPTED',
    `created_at`  DATETIME    NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_invitation_inviter_invitee` (`inviter_id`, `invitee_id`),
    KEY `idx_invitation_code` (`invite_code`),
    KEY `idx_invitation_status` (`status`),
    CONSTRAINT `fk_invitation_inviter` FOREIGN KEY (`inviter_id`) REFERENCES `user` (`id`),
    CONSTRAINT `fk_invitation_invitee` FOREIGN KEY (`invitee_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  couple  (남/녀 각 1명, 양방향 연결)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `couple`;
CREATE TABLE `couple` (
    `id`           BIGINT      NOT NULL AUTO_INCREMENT,
    `male_id`      BIGINT      NOT NULL,
    `female_id`    BIGINT      NOT NULL,
    `status`       VARCHAR(30) NULL DEFAULT 'WAIT' COMMENT 'WAIT / CONNECTED / DISCONNECTED',
    `connected_at` DATETIME    NULL COMMENT '연결 성립 시각',
    `created_at`   DATETIME    NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_couple_male_female` (`male_id`, `female_id`),
    KEY `idx_couple_male` (`male_id`),
    KEY `idx_couple_female` (`female_id`),
    CONSTRAINT `fk_couple_male`   FOREIGN KEY (`male_id`)   REFERENCES `user` (`id`),
    CONSTRAINT `fk_couple_female` FOREIGN KEY (`female_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  terms_agreement  (약관 동의)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `terms_agreement`;
CREATE TABLE `terms_agreement` (
    `id`         BIGINT      NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT      NOT NULL,
    `terms_type` VARCHAR(50) NOT NULL COMMENT 'SERVICE / PRIVACY / FINANCE / MARKETING',
    `agreed`     BOOLEAN     NULL,
    `created_at` DATETIME    NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_terms_user_type` (`user_id`, `terms_type`),
    CONSTRAINT `fk_terms_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  goal  (부부 공동 목표 + 공동 투자성향 - 커플당 1개, 덮어쓰기 / 이력 없음)
--
--  커플 연결 시 status='DRAFT' 로 빈 row 생성 → 온보딩 5단계
--  (목표 설정 / 투자 계획 / 재무 여유 / 위험 반응 / 투자 경험)로 채우고 'ACTIVE'.
--  개인별 투자성향 설문(investment_profile)은 폐기.
--  마이페이지에서 각 값 수정 가능. 목표 달성 시 target_amount/target_date 만
--  다시 입력받아 같은 row 를 갱신한다.
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `goal`;
CREATE TABLE `goal` (
    `id`                        BIGINT      NOT NULL AUTO_INCREMENT,
    `couple_id`                 BIGINT      NOT NULL,

    -- 1/5 목표 설정
    `target_amount`             BIGINT      NULL COMMENT '목표 금액(원)',
    `target_date`               DATE        NULL COMMENT '목표일',
    `investment_period_months`  INT         NULL COMMENT 'target_date 기준 산출 개월수 (투자기간 점수 15점)',

    -- 2/5 투자 계획
    `current_amount`            BIGINT      NULL DEFAULT 0 COMMENT '현재 투자 가능 금액(원) = 시작 자본, 이후 진척 추적',
    `current_amount_updated_at` DATETIME    NULL,
    `monthly_investable_amount` BIGINT      NULL COMMENT '월 투자 가능 금액(원)',

    -- 3/5 재무 여유 (점수 O)
    `emergency_fund_months`     VARCHAR(20) NULL COMMENT '비상자금(생활비 개월수) UNDER_1M / M1_3 / M3_6 / M6_12 / OVER_12M (18점)',
    `monthly_surplus_band`      VARCHAR(20) NULL COMMENT '월 여유자금 비율 UNDER_0 / UNDER_10 / B10_20 / B20_30 / OVER_30 (17점)',

    -- 4/5 위험 반응 (점수 O)
    `max_allowed_loss_rate`     INT         NULL COMMENT '최대 허용손실 0/5/10/20/30/40 (40=30%초과 감수) (25점)',
    `loss_reaction`             VARCHAR(20) NULL COMMENT 'SELL_ALL / SELL_MOST / SELL_PART / HOLD / BUY_MORE (15점)',

    -- 5/5 투자 경험 (점수 O)
    `investment_experience`     VARCHAR(20) NULL COMMENT 'NONE / SAVINGS_ONLY / ETF_ONLY / STOCK_ALL / MULTI_ASSET (10점)',

    -- 산출 결과
    `risk_profile_score`        INT         NULL COMMENT '6문항 공동 위험점수 R (0~100)',
    `joint_risk_profile_type`   VARCHAR(30) NULL COMMENT 'R 점수 구간 → STABLE / STABLE_SEEKING / NEUTRAL / ACTIVE / AGGRESSIVE',

    `status`                    VARCHAR(30) NULL DEFAULT 'DRAFT' COMMENT 'DRAFT(온보딩 전) / ACTIVE / ACHIEVED / CANCELLED',
    `created_at`                DATETIME    NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`                DATETIME    NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_goal_couple` (`couple_id`),
    CONSTRAINT `fk_goal_couple` FOREIGN KEY (`couple_id`) REFERENCES `couple` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  investment_report  (AI 투자 추천 - 목표당 N, 최신 1개)
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `investment_report`;
CREATE TABLE `investment_report` (
    `id`                         BIGINT       NOT NULL AUTO_INCREMENT,
    `goal_id`                    BIGINT       NOT NULL,
    `input_target_amount`        BIGINT       NULL COMMENT '추천 당시 목표 금액',
    `input_current_amount`       BIGINT       NULL COMMENT '추천 당시 현재 마련한 금액',
    `input_target_date`          DATE         NULL COMMENT '추천 당시 목표 시점',
    `input_risk_profile_type`    VARCHAR(20)  NULL COMMENT '추천 당시 커플 공동 투자성향',
    `input_risk_profile_score`   INT          NULL COMMENT '추천 당시 공동 위험점수 R (0~100)',
    `input_max_loss_rate`        INT          NULL COMMENT '추천 당시 허용 최대 손실률(%)',
    `recommended_monthly_amount` BIGINT       NULL COMMENT '매월 필요 투자액(원)',
    `expected_annual_return`     DECIMAL(6,3) NULL COMMENT '기대 연수익률(%)',
    `expected_max_drawdown`      DECIMAL(6,3) NULL COMMENT '예상 최대 낙폭(%)',
    `projected_final_amount`     BIGINT       NULL COMMENT '목표 시점 예상 자산(원)',
    `goal_achievable`            BOOLEAN      NULL COMMENT '목표 달성 가능 여부',
    `allocation`                 JSON         NULL COMMENT '자산배분 [{"asset_type":"BOND","rate":40,"reason":"..."}]',
    `summary_message`            TEXT         NULL COMMENT '추천 한 줄 요약',
    `rationale`                  TEXT         NULL COMMENT '추천 근거 (AI 생성)',
    `raw_response`               JSON         NULL COMMENT 'AI 원본 응답',
    `created_at`                 DATETIME     NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_report_goal` (`goal_id`, `created_at`),
    CONSTRAINT `fk_report_goal` FOREIGN KEY (`goal_id`) REFERENCES `goal` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- =====================================================================
--  자산 도메인 (타 담당 - source_type 문법 오류만 수정)
-- =====================================================================

-- ---------------------------------------------------------------------
--  investment_account
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `investment_account`;
CREATE TABLE `investment_account` (
    `investment_account_id`  BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`                BIGINT       NOT NULL,
    `institution_code`       VARCHAR(50)  NULL,
    `institution_name`       VARCHAR(100) NOT NULL,
    `account_name`           VARCHAR(100) NULL,
    `account_type`           ENUM('BANK','SECURITIES','PENSION','CRYPTO_EXCHANGE','OTHER') NOT NULL,
    `account_number_masked`  VARCHAR(100) NULL,
    `external_account_id`    VARCHAR(255) NULL,
    `source_type`            ENUM('CONNECTED','MANUAL') NOT NULL DEFAULT 'MANUAL' COMMENT '외부 연동 또는 사용자 직접 입력',
    `is_active`              BOOLEAN      NOT NULL DEFAULT TRUE,
    `last_synced_at`         DATETIME     NULL,
    `created_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`             DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`investment_account_id`),
    KEY `idx_account_user` (`user_id`),
    CONSTRAINT `fk_account_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  investment_asset
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `investment_asset`;
CREATE TABLE `investment_asset` (
    `investment_asset_id`     BIGINT        NOT NULL AUTO_INCREMENT,
    `investment_account_id`   BIGINT        NOT NULL,
    `asset_type`              ENUM('STOCK','ETF','FUND','BOND','DEPOSIT','SAVINGS','PENSION','CASH','CRYPTO','OTHER') NOT NULL,
    `risk_level`              ENUM('VERY_LOW','LOW','MEDIUM','HIGH','VERY_HIGH','UNCLASSIFIED') NOT NULL DEFAULT 'UNCLASSIFIED',
    `market_type`             ENUM('DOMESTIC','FOREIGN','NOT_APPLICABLE') NOT NULL DEFAULT 'NOT_APPLICABLE',
    `product_code`            VARCHAR(100)  NULL,
    `product_name`            VARCHAR(200)  NOT NULL,
    `currency_code`           CHAR(3)       NOT NULL DEFAULT 'KRW',
    `quantity`                DECIMAL(24,8) NULL,
    `average_purchase_price`  DECIMAL(19,4) NULL,
    `principal_amount`        DECIMAL(19,2) NOT NULL DEFAULT 0,
    `current_value`           DECIMAL(19,2) NOT NULL DEFAULT 0,
    `evaluation_profit_loss`  DECIMAL(19,2) NOT NULL DEFAULT 0,
    `return_rate`             DECIMAL(10,4) NULL,
    `interest_rate`           DECIMAL(7,4)  NULL,
    `started_at`              DATE          NULL,
    `maturity_date`           DATE          NULL,
    `monthly_payment_amount`  DECIMAL(19,2) NULL,
    `payment_day`             TINYINT       NULL,
    `source_type`             ENUM('CONNECTED','MANUAL') NOT NULL DEFAULT 'MANUAL' COMMENT '외부 연동 또는 사용자 직접 입력',
    `external_asset_id`       VARCHAR(255)  NULL,
    `valuation_base_date`     DATE          NULL,
    `last_synced_at`          DATETIME      NULL,
    `is_active`               BOOLEAN       NOT NULL DEFAULT TRUE,
    `created_at`              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`              DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`investment_asset_id`),
    KEY `idx_asset_account` (`investment_account_id`),
    CONSTRAINT `fk_asset_account` FOREIGN KEY (`investment_account_id`) REFERENCES `investment_account` (`investment_account_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ---------------------------------------------------------------------
--  investment_asset_snapshot
-- ---------------------------------------------------------------------
DROP TABLE IF EXISTS `investment_asset_snapshot`;
CREATE TABLE `investment_asset_snapshot` (
    `snapshot_id`            BIGINT        NOT NULL AUTO_INCREMENT,
    `investment_asset_id`    BIGINT        NOT NULL,
    `snapshot_date`          DATE          NOT NULL,
    `quantity`               DECIMAL(24,8) NULL,
    `principal_amount`       DECIMAL(19,2) NOT NULL DEFAULT 0,
    `current_value`          DECIMAL(19,2) NOT NULL DEFAULT 0,
    `evaluation_profit_loss` DECIMAL(19,2) NOT NULL DEFAULT 0,
    `return_rate`            DECIMAL(10,4) NULL,
    `created_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`snapshot_id`),
    KEY `idx_snapshot_asset_date` (`investment_asset_id`, `snapshot_date`),
    CONSTRAINT `fk_snapshot_asset` FOREIGN KEY (`investment_asset_id`) REFERENCES `investment_asset` (`investment_asset_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

SET FOREIGN_KEY_CHECKS = 1;
