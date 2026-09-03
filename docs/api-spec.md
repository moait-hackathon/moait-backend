# API 명세서 — 사용자 온보딩 / 부부 연결 / 공동 목표

담당 범위 통합 명세. 관련 ERD: [erd-user-couple-goal.md](erd-user-couple-goal.md)

## 공통

| 항목 | 값 |
|---|---|
| Base URL | `/api` |
| 인증 | JWT — `Authorization: Bearer {accessToken}` |
| Content-Type | `application/json` |
| 시간 포맷 | ISO-8601 (`2026-09-02T10:00:00+09:00`) |
| 금액 단위 | 원 (정수) |

**공통 응답 래퍼** (팀 규격 — 성공/실패 스키마가 다름)

성공:
```json
{ "success": true, "message": "요청이 처리되었습니다.", "data": { /* 엔드포인트별 페이로드 */ } }
```

실패 (모든 실패 응답이 이 세 필드로 고정):
```json
{ "success": false, "errorCode": "DUPLICATE_PHONE", "message": "이미 가입된 휴대폰 번호입니다." }
```

입력값 검증 실패 시 `message` 에는 **첫 번째 필드 에러 메시지**가 담긴다:
```json
{ "success": false, "errorCode": "INVALID_INPUT", "message": "올바른 휴대폰 번호 형식이 아닙니다." }
```

- 실패 응답에 `data` / `errors` 필드는 없다. `success` + HTTP status로 성공/실패 1차 판단, body의 `errorCode`로 상세 분기.
- `message`는 사용자 노출 가능하나, 검증 메시지 일부는 기본 문구(로케일에 따라 영문)일 수 있으므로 화면 문구는 프론트가 `errorCode` 기준으로 관리 권장.

> **이하 각 엔드포인트의 `### Example`은 성공 응답의 `data` 필드 내용만** 표기 (`{ success, message, data }` 래퍼 생략). 실패 응답은 `### Status` 표의 `errorCode` 참고.

**표 표기** — Request/Response 표의 `Nullable` 열: `X` = 필수, `O` = 선택(없어도 됨). Response 표의 key는 `data` 내부 기준.

---

# 엔드포인트 목록

## 인증

> 약관 종류는 프론트 고정 (→ [부록: 약관 종류 정의](#약관-종류-정의)).

| 기능 | 사용자 | Method | URL | param | 설명 |
|---|---|---|---|---|---|
| 회원가입 (LOCAL) | 게스트 | POST | `/auth/signup` | | 휴대폰 중복 확인 + 약관 동의 포함 |
| 소셜 회원가입/로그인 | 게스트 | POST | `/auth/social/{provider}` | `provider` | 카카오/네이버 (신규 시 약관 동의 포함) |
| 로그인 | 게스트 | POST | `/auth/login` | | 휴대폰번호/비밀번호 로그인 |

## 온보딩

> 약관 동의는 회원가입 요청에 포함. 설문 문항은 프론트 고정 (→ [부록: 투자성향 설문 문항 정의](#투자성향-설문-문항-정의)). 온보딩 진행 위치는 회원가입/로그인 응답의 `onboardingStep`으로 판단 (별도 상태 조회 API 없음).

| 기능 | 사용자 | Method | URL | param | 설명 |
|---|---|---|---|---|---|
| 기본 재무정보 입력 | 회원 | PATCH | `/onboarding/financial-info` | | 연소득/총자산 |
| 투자성향 설문 제출 | 회원 | POST | `/onboarding/investment-profile` | | 설문 제출 → 성향 유형 산출 |

## 부부 연결

> 초대 코드는 **회원가입 시 자동 발급** (사용자당 1개, `invitation` 마스터 row, 만료 없음). 연결은 **양방향** — 두 사람이 서로의 코드를 입력하거나, 한쪽이 요청 → 다른 쪽이 수락. 남/녀 각 1명이어야 연결 가능. (알림 미구현 — 상대는 `GET /couples/status`로 요청 확인)

| 기능 | 사용자 | Method | URL | param | 설명 |
|---|---|---|---|---|---|
| 내 초대 코드 조회 | 회원 | GET | `/couples/invite-code` | | 가입 시 자동 발급된 코드 |
| 연결 요청 / 확정 | 회원 | POST | `/couples/connect` | | 상대 코드 입력 → 요청, 상대도 했으면 즉시 연결 |
| 연결 요청 수락 | 회원 | POST | `/couples/requests/{partnerUserId}/accept` | `partnerUserId` | 나에게 온 요청 수락 → 연결 |
| 내 연결 상태 목록 | 회원 | GET | `/couples/status` | | 대기/요청/연결 상태 목록 (동시 다수 가능) |
| 연결된 커플 조회 | 회원 | GET | `/couples/me` | | `CONNECTED` 커플 + 파트너 정보 |
| 연결 해제 | 회원 | DELETE | `/couples/me` | | 커플 연결 해제 |

## 공동 목표

| 기능 | 사용자 | Method | URL | param | 설명 |
|---|---|---|---|---|---|
| 공동 목표 생성 | 커플 | POST | `/goals` | | 커플당 1개 |
| 공동 목표 조회 | 커플 | GET | `/goals/me` | | 목표+진척률 |
| 공동 목표 수정 | 커플 | PATCH | `/goals/me` | | 금액/시점/손실률 |
| 현재 마련한 금액 갱신 | 커플 | PATCH | `/goals/me/current-amount` | | 진척 금액 갱신 |
| 공동 투자성향 재계산 | 커플 | POST | `/goals/me/recalculate-risk-profile` | | 파트너 재설문 반영 |
| 공동 목표 취소 | 커플 | DELETE | `/goals/me` | | soft delete |

---

# 인증

## 회원가입 (LOCAL)

`POST /api/auth/signup`

휴대폰번호가 기존 회원과 겹치지 않으면 그대로 가입 처리. 별도 SMS 인증 절차 없음. 약관 동의를 요청에 포함.

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| name | 이름 | String | 1~50자 | X | `"홍길동"` |
| phone | 휴대폰번호 (숫자만) | String | 10~11자리, 중복 불가 | X | `"01012345678"` |
| password | 비밀번호 | String | 8자↑, 영문+숫자+특수 | X | `"P@ssw0rd!"` |
| gender | 성별 | String | `MALE` `FEMALE` | X | `"MALE"` |
| agreements | 약관 동의 목록 | Array | 필수 약관 전부 `true` 필요 | X | |
| agreements[].termsType | 약관 코드 | String | `SERVICE` `PRIVACY` `FINANCE` `MARKETING` | X | `"SERVICE"` |
| agreements[].agreed | 동의 여부 | Boolean | | X | `true` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| userId | 생성된 사용자 ID | Number | | X | `1001` |
| accessToken | 액세스 토큰 | String | | X | `"eyJ..."` |
| onboardingStep | 다음 진행 단계 | String | `FINANCIAL_INFO` `INVESTMENT_PROFILE` `DONE` | X | `"FINANCIAL_INFO"` |

### Example

```json
// Request
{
  "name": "홍길동",
  "phone": "01012345678",
  "password": "P@ssw0rd!",
  "gender": "MALE",
  "agreements": [
    { "termsType": "SERVICE", "agreed": true },
    { "termsType": "PRIVACY", "agreed": true },
    { "termsType": "FINANCE", "agreed": true },
    { "termsType": "MARKETING", "agreed": false }
  ]
}

// Response 201  (data)
{ "userId": 1001, "accessToken": "eyJ...", "onboardingStep": "FINANCIAL_INFO" }
```

가입 시 서버에서 초대 코드(`invitation` 마스터 row) 자동 생성, `terms_agreement` 저장.

### Status

| status | response content |
|---|---|
| 201 | 가입 성공 |
| 400 | `INVALID_INPUT` — 검증 실패 |
| 409 | `DUPLICATE_PHONE` — 이미 가입된 번호 |
| 422 | `TERMS_REQUIRED_NOT_AGREED` — 필수 약관 미동의 |

---

## 소셜 회원가입/로그인

`POST /api/auth/social/{provider}`

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| authorizationCode | 소셜 인가 코드 | String | | X | `"aXbYcZ..."` |
| gender | 성별 | String | `MALE` `FEMALE` — **신규 가입 시 필수** | O | `"MALE"` |
| agreements | 약관 동의 목록 | Array | **신규 가입 시 필수**, 기존 회원은 무시 | O | |
| agreements[].termsType | 약관 코드 | String | `SERVICE` `PRIVACY` `MARKETING` | O | `"SERVICE"` |
| agreements[].agreed | 동의 여부 | Boolean | | O | `true` |

### Query parameter

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| provider | 소셜 종류 (path) | String | `kakao` `naver` | X | `kakao` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| userId | 사용자 ID | Number | | X | `1002` |
| accessToken | 액세스 토큰 | String | | X | `"eyJ..."` |
| isNewUser | 신규 가입 여부 | Boolean | | X | `true` |
| onboardingStep | 다음 진행 단계 | String | | X | `"FINANCIAL_INFO"` |

### Example

```json
// Request (신규)
{
  "authorizationCode": "aXbYcZ...",
  "gender": "MALE",
  "agreements": [
    { "termsType": "SERVICE", "agreed": true },
    { "termsType": "PRIVACY", "agreed": true },
    { "termsType": "FINANCE", "agreed": true },
    { "termsType": "MARKETING", "agreed": false }
  ]
}

// Response 201  (data)
{ "userId": 1002, "accessToken": "eyJ...", "isNewUser": true, "onboardingStep": "FINANCIAL_INFO" }
```

### Status

| status | response content |
|---|---|
| 200 | 기존 회원 로그인 |
| 201 | 신규 회원 가입 |
| 400 | `SOCIAL_AUTH_FAILED` — 인가 코드 검증 실패 |
| 422 | `TERMS_REQUIRED_NOT_AGREED` — 신규인데 필수 약관 미동의 |

---

## 로그인

`POST /api/auth/login`

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| phone | 휴대폰번호 | String | | X | `"01012345678"` |
| password | 비밀번호 | String | | X | `"P@ssw0rd!"` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| userId | 사용자 ID | Number | | X | `1001` |
| accessToken | 액세스 토큰 | String | | X | `"eyJ..."` |
| onboardingStep | 다음 진행 단계 | String | | X | `"DONE"` |

### Example

```json
// Request
{ "phone": "01012345678", "password": "P@ssw0rd!" }

// Response 200
{ "userId": 1001, "accessToken": "eyJ...", "onboardingStep": "DONE" }
```

### Status

| status | response content |
|---|---|
| 200 | 로그인 성공 |
| 401 | `INVALID_CREDENTIALS` — 아이디/비밀번호 불일치 |

---

# 온보딩

## 기본 재무정보 입력

`PATCH /api/onboarding/financial-info` · 인증 필요

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| annualIncome | 연소득(원) | Number | 0 이상 | X | `45000000` |
| totalAsset | 총 자산(원) | Number | 0 이상 | X | `30000000` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| onboardingStep | 다음 단계 | String | | X | `"INVESTMENT_PROFILE"` |

### Example

```json
// Request
{ "annualIncome": 45000000, "totalAsset": 30000000 }

// Response 200
{ "onboardingStep": "INVESTMENT_PROFILE" }
```

### Status

| status | response content |
|---|---|
| 200 | 저장 성공 |
| 400 | `INVALID_INPUT` — 음수 등 |
| 401 | `UNAUTHORIZED` |

---

## 투자성향 설문 제출

`POST /api/onboarding/investment-profile` · 인증 필요

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| investmentExperience | 투자 경험 | String | `NONE` `SAVINGS_ONLY` `ETF_ONLY` `STOCK_ALL` `ETC` | X | `"ETF_ONLY"` |
| lossReaction | 손실 대응 방식 | String | `SELL_ALL` `SELL_PART` `HOLD` `BUY_MORE` | X | `"HOLD"` |
| maxTolerableLossRate | 최대 감내 손실률(%) | Number | 0~100 | X | `15` |
| investmentHorizon | 투자 가능 기간 | String | `UNDER_1Y` `Y1_3` `Y3_5` `OVER_5Y` | X | `"Y3_5"` |
| holdingAssets | 보유 투자자산 목록 | Array | 빈 배열 허용 | X | |
| holdingAssets[].type | 자산군 | String | `DEPOSIT` `ETF` `STOCK` … | X | `"ETF"` |
| holdingAssets[].amount | 금액(원) | Number | 0 이상 | X | `5000000` |
| monthlyInvestableAmount | 매월 투자 가능액(원) | Number | 0 이상 | X | `500000` |
| emergencyFundSecured | 비상자금 확보 여부 | Boolean | | X | `true` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| investmentProfileId | 프로필 ID | Number | | X | `55` |
| riskProfileType | 산출된 투자성향 유형 | String | `STABLE` `STABLE_SEEKING` `NEUTRAL` `ACTIVE` `AGGRESSIVE` | X | `"NEUTRAL"` |
| riskProfileTypeLabel | 유형 한글명 | String | | X | `"위험중립형"` |
| riskProfileScore | 산출 점수 | Number | | X | `58` |
| summary | 성향 요약 문구 | String | | O | `"..."` |
| onboardingStep | 다음 단계 | String | | X | `"DONE"` |

### Example

```json
// Request
{
  "investmentExperience": "ETF_ONLY",
  "lossReaction": "HOLD",
  "maxTolerableLossRate": 15,
  "investmentHorizon": "Y3_5",
  "holdingAssets": [
    { "type": "DEPOSIT", "amount": 10000000 },
    { "type": "ETF", "amount": 5000000 }
  ],
  "monthlyInvestableAmount": 500000,
  "emergencyFundSecured": true
}

// Response 200  (data)
{
  "investmentProfileId": 55,
  "riskProfileType": "NEUTRAL",
  "riskProfileTypeLabel": "위험중립형",
  "riskProfileScore": 58,
  "summary": "손실을 어느 정도 감내하며 중기 목표에 맞춰 분산 투자하는 성향입니다.",
  "onboardingStep": "DONE"
}
```

### Status

| status | response content |
|---|---|
| 200 | 제출/산출 성공 (재설문 시 기존 `is_latest=false`) |
| 400 | `INVALID_INPUT` — enum/범위 위반 |
| 401 | `UNAUTHORIZED` |

---

# 부부 연결

> **연결 상태값**
> - `WAIT` — 내가 상대 코드를 입력함, 상대의 응답(코드 입력 or 수락) 대기
> - `REQUESTED` — 상대가 내 코드를 입력함, 내 수락 대기
> - `CONNECTED` — 연결 완료
> - `DISCONNECTED` — 해제됨 (이력)
>
> 연결 성립 조건: 서로 본인 코드 아님 + **성별이 남/녀로 다름** + 둘 다 다른 사람과 `CONNECTED` 아님. `couple`의 `male_id`/`female_id`는 성별로 배정.

## 내 초대 코드 조회

`GET /api/couples/invite-code` · 인증 필요

회원가입 시 자동 발급된 초대 코드.

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| inviteCode | 내 초대 코드 | String | 6자리 영숫자 | X | `"8F3K2Q"` |
| shareUrl | 공유 URL | String | | X | `"https://moait.app/invite/8F3K2Q"` |

### Example

```json
// Response 200
{ "inviteCode": "8F3K2Q", "shareUrl": "https://moait.app/invite/8F3K2Q" }
```

### Status

| status | response content |
|---|---|
| 200 | 조회 성공 |

---

## 연결 요청 / 확정

`POST /api/couples/connect` · 인증 필요

상대의 초대 코드를 입력. **상대가 아직 안 했으면 요청(`WAIT`) 생성. 상대가 이미 내 코드를 입력해둔 상태(`REQUESTED`)면 즉시 `CONNECTED`.**

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| inviteCode | 상대 초대 코드 | String | 6자리 | X | `"8F3K2Q"` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| coupleId | 커플 ID | Number | | X | `200` |
| status | 결과 상태 | String | `WAIT` `CONNECTED` | X | `"WAIT"` |
| partner.userId | 상대(코드 소유자) ID | Number | | X | `1001` |
| partner.name | 상대 이름 | String | | X | `"홍길동"` |
| partner.gender | 상대 성별 | String | `MALE` `FEMALE` | X | `"MALE"` |
| connectedAt | 연결 시각 (`CONNECTED`일 때만) | String(datetime) | | O | `"2026-09-02T11:00:00+09:00"` |

### Example

```json
// Request
{ "inviteCode": "8F3K2Q" }

// Response 200 (상대 대기)
{ "coupleId": 200, "status": "WAIT", "partner": { "userId": 1001, "name": "홍길동", "gender": "MALE" } }

// Response 200 (즉시 연결 — 상대가 이미 내 코드 입력해둠)
{ "coupleId": 200, "status": "CONNECTED", "connectedAt": "2026-09-02T11:00:00+09:00", "partner": { "userId": 1001, "name": "홍길동", "gender": "MALE" } }
```

### Status

| status | response content |
|---|---|
| 200 | 요청 생성 또는 연결 완료 |
| 403 | `ONBOARDING_NOT_COMPLETED` |
| 404 | `INVITATION_NOT_FOUND` — 존재하지 않는 코드 |
| 409 | `CANNOT_CONNECT_SELF` — 본인 코드 |
| 409 | `SAME_GENDER` — 두 사람 성별이 같음 |
| 409 | `ALREADY_CONNECTED` — 나 또는 상대가 이미 다른 사람과 연결됨 |

---

## 연결 요청 수락

`POST /api/couples/requests/{partnerUserId}/accept` · 인증 필요

나에게 온 요청(`REQUESTED`) 중 지정한 상대의 요청을 수락 → `CONNECTED`. (`GET /couples/status`에서 확인한 요청을 상대 코드 입력 없이 수락하는 경로)

### Request
없음 (바디 없음)

### Query parameter

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| partnerUserId | 수락할 상대의 userId (path) | Number | | X | `1001` |

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| coupleId | 커플 ID | Number | | X | `200` |
| status | 커플 상태 | String | `CONNECTED` | X | `"CONNECTED"` |
| connectedAt | 연결 시각 | String(datetime) | | X | `"2026-09-02T11:00:00+09:00"` |
| partner.userId | 파트너 ID | Number | | X | `1001` |
| partner.name | 파트너 이름 | String | | X | `"홍길동"` |
| partner.gender | 파트너 성별 | String | | X | `"MALE"` |

### Example

```json
// Response 200  (data)
{
  "coupleId": 200,
  "status": "CONNECTED",
  "connectedAt": "2026-09-02T11:00:00+09:00",
  "partner": { "userId": 1001, "name": "홍길동", "gender": "MALE" }
}
```

### Status

| status | response content |
|---|---|
| 200 | 연결 성공 |
| 403 | `ONBOARDING_NOT_COMPLETED` |
| 404 | `REQUEST_NOT_FOUND` — 해당 상대의 대기 중 요청 없음 |
| 409 | `ALREADY_CONNECTED` — 나 또는 상대가 이미 다른 사람과 연결됨 |

---

## 내 연결 상태 목록

`GET /api/couples/status` · 인증 필요

내가 관여된 모든 연결 건 (동시에 여러 명에게 요청/수신 가능하므로 **배열**). `DISCONNECTED`는 제외.

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| (root) | 연결 건 목록 | Array | | X | |
| [].coupleId | 커플 ID | Number | | X | `200` |
| [].status | 상태 | String | `WAIT` `REQUESTED` `CONNECTED` | X | `"REQUESTED"` |
| [].partner.userId | 상대 ID | Number | | X | `1001` |
| [].partner.name | 상대 이름 | String | | X | `"홍길동"` |
| [].partner.gender | 상대 성별 | String | | X | `"MALE"` |

### Example

```json
// Response 200  (data)
[
  { "coupleId": 200, "status": "REQUESTED", "partner": { "userId": 1001, "name": "홍길동", "gender": "MALE" } },
  { "coupleId": 201, "status": "WAIT",      "partner": { "userId": 1005, "name": "이철수", "gender": "MALE" } }
]
```

### Status

| status | response content |
|---|---|
| 200 | 조회 성공 (없으면 빈 배열) |

---

## 연결된 커플 조회

`GET /api/couples/me` · 인증 필요

`CONNECTED` 상태의 커플 + 파트너 정보. 공동 목표 등 커플 기능의 진입 확인용.

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| coupleId | 커플 ID | Number | | X | `200` |
| status | 커플 상태 | String | `CONNECTED` | X | `"CONNECTED"` |
| connectedAt | 연결 시각 | String(datetime) | | X | `"2026-09-02T11:00:00+09:00"` |
| me.userId | 내 ID | Number | | X | `1002` |
| me.name | 내 이름 | String | | X | `"김영희"` |
| me.gender | 내 성별 | String | `MALE` `FEMALE` | X | `"FEMALE"` |
| me.riskProfileType | 내 투자성향 | String | | O | `"STABLE"` |
| partner.userId | 파트너 ID | Number | | X | `1001` |
| partner.name | 파트너 이름 | String | | X | `"홍길동"` |
| partner.gender | 파트너 성별 | String | | X | `"MALE"` |
| partner.riskProfileType | 파트너 투자성향 | String | | O | `"NEUTRAL"` |

### Example

```json
// Response 200  (data)
{
  "coupleId": 200,
  "status": "CONNECTED",
  "connectedAt": "2026-09-02T11:00:00+09:00",
  "me": { "userId": 1002, "name": "김영희", "gender": "FEMALE", "riskProfileType": "STABLE" },
  "partner": { "userId": 1001, "name": "홍길동", "gender": "MALE", "riskProfileType": "NEUTRAL" }
}
```

### Status

| status | response content |
|---|---|
| 200 | 조회 성공 |
| 404 | `COUPLE_NOT_FOUND` — 연결된 커플 없음 |

---

## 연결 해제

`DELETE /api/couples/me` · 인증 필요

`CONNECTED` 커플을 `DISCONNECTED`로 전환. row는 삭제하지 않음.

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| coupleId | 커플 ID | Number | | X | `200` |
| status | 변경된 상태 | String | `DISCONNECTED` | X | `"DISCONNECTED"` |

### Example

```json
// Response 200
{ "coupleId": 200, "status": "DISCONNECTED" }
```

### Status

| status | response content |
|---|---|
| 200 | 해제 성공 (soft, 이력 보존) |
| 404 | `COUPLE_NOT_FOUND` |
| 409 | `NOT_CONNECTED` — CONNECTED 상태 아님 |

---

# 공동 목표

## 공동 목표 생성

`POST /api/goals` · 인증 필요 (커플 연결 상태)

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| targetAmount | 목표 금액(원) | Number | 1 이상 | X | `50000000` |
| currentAmount | 현재 마련한 금액(원) | Number | 0 이상 | X | `12000000` |
| targetDate | 목표 시점 | String(date) | 오늘 이후 | X | `"2028-06-30"` |
| maxAllowedLossRate | 공동 허용 최대 손실률(%) | Number | 0~100 | X | `10` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| goalId | 목표 ID | Number | | X | `400` |
| coupleId | 커플 ID | Number | | X | `200` |
| targetAmount | 목표 금액 | Number | | X | `50000000` |
| currentAmount | 현재 금액 | Number | | X | `12000000` |
| currentAmountUpdatedAt | 현재 금액 갱신 시각 | String(datetime) | | X | `"2026-09-02T12:00:00+09:00"` |
| targetDate | 목표 시점 | String(date) | | X | `"2028-06-30"` |
| maxAllowedLossRate | 허용 손실률(%) | Number | | X | `10` |
| jointRiskProfileType | 커플 공동 투자성향 | String | `STABLE` `STABLE_SEEKING` `NEUTRAL` `ACTIVE` `AGGRESSIVE` | X | `"STABLE_SEEKING"` |
| jointRiskProfileTypeLabel | 공동 성향 한글명 | String | | X | `"안정추구형"` |
| status | 목표 상태 | String | `ACTIVE` `ACHIEVED` `CANCELLED` | X | `"ACTIVE"` |
| progress.rate | 달성률(%) | Number | | X | `24.0` |
| progress.remainingAmount | 남은 금액(원) | Number | | X | `38000000` |
| progress.remainingMonths | 남은 개월수 | Number | | X | `21` |
| progress.requiredMonthlyAmount | 월 필요 금액(원) | Number | | X | `1809524` |
| createdAt | 생성 시각 | String(datetime) | | X | `"2026-09-02T12:00:00+09:00"` |

### Example

```json
// Request
{ "targetAmount": 50000000, "currentAmount": 12000000, "targetDate": "2028-06-30", "maxAllowedLossRate": 10 }

// Response 201  (data)
{
  "goalId": 400, "coupleId": 200,
  "targetAmount": 50000000, "currentAmount": 12000000,
  "currentAmountUpdatedAt": "2026-09-02T12:00:00+09:00",
  "targetDate": "2028-06-30", "maxAllowedLossRate": 10,
  "jointRiskProfileType": "STABLE_SEEKING", "jointRiskProfileTypeLabel": "안정추구형",
  "status": "ACTIVE",
  "progress": { "rate": 24.0, "remainingAmount": 38000000, "remainingMonths": 21, "requiredMonthlyAmount": 1809524 },
  "createdAt": "2026-09-02T12:00:00+09:00"
}
```

### Status

| status | response content |
|---|---|
| 201 | 생성 성공 |
| 400 | `INVALID_TARGET_DATE` — 목표 시점 과거 |
| 409 | `COUPLE_NOT_CONNECTED` |
| 409 | `GOAL_ALREADY_EXISTS` — 이미 목표 있음 (PATCH 사용) |
| 422 | `PARTNER_PROFILE_INCOMPLETE` — 파트너 설문 미완료 |

---

## 공동 목표 조회

`GET /api/goals/me` · 인증 필요

### Request
없음

### Query parameter
없음

### Response

`공동 목표 생성` 응답과 동일 + 아래

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| hasReport | AI 추천(investment_report) 존재 여부 | Boolean | | X | `true` |
| updatedAt | 수정 시각 | String(datetime) | | O | `"2026-10-01T09:00:00+09:00"` |

### Example

```json
// Response 200  (data)
{
  "goalId": 400, "coupleId": 200,
  "targetAmount": 50000000, "currentAmount": 15000000,
  "currentAmountUpdatedAt": "2026-10-01T09:00:00+09:00",
  "targetDate": "2028-06-30", "maxAllowedLossRate": 10,
  "jointRiskProfileType": "STABLE_SEEKING", "status": "ACTIVE",
  "progress": { "rate": 30.0, "remainingAmount": 35000000, "remainingMonths": 20, "requiredMonthlyAmount": 1750000 },
  "hasReport": true,
  "createdAt": "2026-09-02T12:00:00+09:00",
  "updatedAt": "2026-10-01T09:00:00+09:00"
}
```

### Status

| status | response content |
|---|---|
| 200 | 조회 성공 |
| 404 | `GOAL_NOT_FOUND` |

---

## 공동 목표 수정

`PATCH /api/goals/me` · 인증 필요

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| targetAmount | 목표 금액(원) | Number | 1 이상 | O | `60000000` |
| targetDate | 목표 시점 | String(date) | 오늘 이후 | O | `"2029-06-30"` |
| maxAllowedLossRate | 허용 손실률(%) | Number | 0~100, 변경 시 공동성향 재계산 | O | `15` |

> `currentAmount`, `status`는 이 API로 수정 불가.

### Query parameter
없음

### Response
`공동 목표 조회`와 동일 구조 (갱신 반영)

### Example

```json
// Request
{ "targetAmount": 60000000, "targetDate": "2029-06-30", "maxAllowedLossRate": 15 }

// Response 200 — 갱신된 목표 전체
```

### Status

| status | response content |
|---|---|
| 200 | 수정 성공 |
| 400 | `INVALID_TARGET_DATE` |
| 404 | `GOAL_NOT_FOUND` |
| 409 | `GOAL_NOT_ACTIVE` — 취소/달성 목표 |

---

## 현재 마련한 금액 갱신

`PATCH /api/goals/me/current-amount` · 인증 필요

### Request

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| currentAmount | 현재 마련한 금액(원) | Number | 0 이상 | X | `15000000` |

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| goalId | 목표 ID | Number | | X | `400` |
| currentAmount | 갱신된 금액 | Number | | X | `15000000` |
| currentAmountUpdatedAt | 갱신 시각 | String(datetime) | | X | `"2026-10-01T09:00:00+09:00"` |
| status | 목표 상태 (달성 시 `ACHIEVED` 자동 전환) | String | | X | `"ACTIVE"` |
| progress.rate | 달성률(%) | Number | | X | `30.0` |
| progress.remainingAmount | 남은 금액 | Number | | X | `35000000` |
| progress.remainingMonths | 남은 개월수 | Number | | X | `20` |
| progress.requiredMonthlyAmount | 월 필요 금액 | Number | | X | `1750000` |

### Example

```json
// Request
{ "currentAmount": 15000000 }

// Response 200  (data)
{
  "goalId": 400, "currentAmount": 15000000,
  "currentAmountUpdatedAt": "2026-10-01T09:00:00+09:00",
  "status": "ACTIVE",
  "progress": { "rate": 30.0, "remainingAmount": 35000000, "remainingMonths": 20, "requiredMonthlyAmount": 1750000 }
}
```

### Status

| status | response content |
|---|---|
| 200 | 갱신 성공 |
| 404 | `GOAL_NOT_FOUND` |
| 409 | `GOAL_NOT_ACTIVE` |

---

## 공동 투자성향 재계산

`POST /api/goals/me/recalculate-risk-profile` · 인증 필요

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| goalId | 목표 ID | Number | | X | `400` |
| jointRiskProfileType | 재계산된 공동 성향 | String | | X | `"NEUTRAL"` |
| jointRiskProfileTypeLabel | 한글명 | String | | X | `"위험중립형"` |

### Example

```json
// Response 200
{ "goalId": 400, "jointRiskProfileType": "NEUTRAL", "jointRiskProfileTypeLabel": "위험중립형" }
```

### Status

| status | response content |
|---|---|
| 200 | 재계산 성공 |
| 404 | `GOAL_NOT_FOUND` |
| 422 | `PARTNER_PROFILE_INCOMPLETE` |

---

## 공동 목표 취소

`DELETE /api/goals/me` · 인증 필요

### Request
없음

### Query parameter
없음

### Response

| key | 설명 | value 타입 | 옵션 | Nullable | 예시 |
|---|---|---|---|---|---|
| goalId | 목표 ID | Number | | X | `400` |
| status | 변경된 상태 | String | `CANCELLED` | X | `"CANCELLED"` |

### Example

```json
// Response 200
{ "goalId": 400, "status": "CANCELLED" }
```

### Status

| status | response content |
|---|---|
| 200 | 취소 성공 (soft delete, 이력 보존) |
| 404 | `GOAL_NOT_FOUND` |
| 409 | `GOAL_NOT_ACTIVE` — 이미 취소/달성 |

---

# 부록

## progress 계산 규칙

```
rate                  = round(currentAmount / targetAmount * 100, 1)
remainingAmount       = max(targetAmount - currentAmount, 0)
remainingMonths       = ceil(오늘 ~ targetDate 개월수)
requiredMonthlyAmount = remainingMonths > 0 ? ceil(remainingAmount / remainingMonths) : remainingAmount
```

## onboardingStep 값

| 값 | 의미 |
|---|---|
| `FINANCIAL_INFO` | 기본 재무정보 필요 |
| `INVESTMENT_PROFILE` | 투자성향 설문 필요 |
| `DONE` | 온보딩 완료 |

## 투자성향 유형 (riskProfileType / jointRiskProfileType)

| 코드 | 한글명 |
|---|---|
| `STABLE` | 안정형 |
| `STABLE_SEEKING` | 안정추구형 |
| `NEUTRAL` | 위험중립형 |
| `ACTIVE` | 적극투자형 |
| `AGGRESSIVE` | 공격투자형 |

## 약관 종류 정의

프론트 고정. 회원가입 시 `agreements: [{ termsType, agreed }]`로 전송. 필수 약관은 전부 `agreed=true`여야 가입 가능 (아니면 `422 TERMS_REQUIRED_NOT_AGREED`).

| termsType | 이름 | 필수 | 비고 |
|---|---|---|---|
| `SERVICE` | 서비스 이용약관 | ✔ | |
| `PRIVACY` | 개인정보 수집·이용 동의 | ✔ | |
| `FINANCE` | 금융정보 연동 약관 | ✔ | 계좌·카드 연동(CODEF) 전제 |
| `MARKETING` | 마케팅 정보 수신 동의 | | 선택 |

전문 URL은 프론트 정적 페이지로 관리.

## 투자성향 설문 문항 정의

프론트가 이 정의대로 화면을 구성하고, 제출 시 `value` 코드를 `POST /onboarding/investment-profile`로 전송. (조회 API 없음 — 프론트/백엔드 공통 계약)

### 1. investmentExperience — "투자 경험이 어느 정도인가요?" (단일 선택)

| value | label |
|---|---|
| `NONE` | 없다 |
| `SAVINGS_ONLY` | 예적금만 해봤다 |
| `ETF_ONLY` | ETF만 해봤다 |
| `STOCK_ALL` | 주식까지 다 해봤다 |
| `ETC` | 기타 |

### 2. lossReaction — "투자 손실이 발생하면 어떻게 대응하나요?" (단일 선택)

| value | label |
|---|---|
| `SELL_ALL` | 전량 매도한다 |
| `SELL_PART` | 일부 매도한다 |
| `HOLD` | 그대로 보유한다 |
| `BUY_MORE` | 추가 매수한다 |

### 3. maxTolerableLossRate — "감당할 수 있는 최대 손실률은?" (단일 선택, Number)

| value | label |
|---|---|
| `0` | 원금 손실은 안 된다 |
| `5` | -5% 이내 |
| `10` | -10% 이내 |
| `20` | -20% 이내 |
| `30` | -30% 이내 |
| `40` | -30% 이상도 감내 |

### 4. investmentHorizon — "투자 가능 기간은?" (단일 선택)

| value | label |
|---|---|
| `UNDER_1Y` | 1년 미만 |
| `Y1_3` | 1~3년 |
| `Y3_5` | 3~5년 |
| `OVER_5Y` | 5년 이상 |

### 5. holdingAssets — "현재 보유한 투자자산과 금액을 입력해주세요." (자산군별 금액 입력)

자산군: `DEPOSIT` `SAVINGS` `BOND` `FUND` `ETF` `STOCK` `PENSION` `CRYPTO` `CASH` `OTHER`
제출 형식: `[{ "type": "ETF", "amount": 5000000 }, ...]` (빈 배열 허용)

### 6. monthlyInvestableAmount — "매월 투자에 쓸 수 있는 금액은?" (금액 입력, 원)

### 7. emergencyFundSecured — "비상자금(3~6개월 생활비)을 확보하고 있나요?" (예/아니오, Boolean)

## 투자성향 점수 산출 (MVP 초기 가설)

`POST /onboarding/investment-profile` 제출 시 서버가 계산. **`RiskProfileCalculator`에 상수로 관리** — 팀이 숫자만 튜닝.

**총점 100 = Q3(35) + Q2(25) + Q4(20) + Q1(12) + Q7(8).** Q5·Q6은 점수 없음(저장만, R/G·서비스 안전상한 계산에 추후 사용).

### Q3 maxTolerableLossRate → 35

| 입력값 | 점수 |
|---|---|
| 0 | 0 |
| 1~5 | 8 |
| 6~10 | 15 |
| 11~20 | 25 |
| 21~30 | 32 |
| 31+ | 35 |

### Q2 lossReaction → 25

| value | 점수 |
|---|---|
| `SELL_ALL` | 0 |
| `SELL_PART` | 8 |
| `HOLD` | 18 |
| `BUY_MORE` | 25 |

### Q4 investmentHorizon → 20

| value | 점수 |
|---|---|
| `UNDER_1Y` | 0 |
| `Y1_3` | 7 |
| `Y3_5` | 14 |
| `OVER_5Y` | 20 |

### Q1 investmentExperience → 12

| value | 점수 |
|---|---|
| `NONE` | 0 |
| `SAVINGS_ONLY` | 3 |
| `ETF_ONLY` | 7 |
| `STOCK_ALL` | 12 |
| `ETC` | 3 |

### Q7 emergencyFundSecured → 8

| value | 점수 |
|---|---|
| `true` | 8 |
| `false` | 0 |

### 점수 → riskProfileType

| 점수 | riskProfileType | label |
|---|---|---|
| 0 ~ 20 | `STABLE` | 안정형 |
| 21 ~ 40 | `STABLE_SEEKING` | 안정추구형 |
| 41 ~ 60 | `NEUTRAL` | 위험중립형 |
| 61 ~ 80 | `ACTIVE` | 적극투자형 |
| 81 ~ 100 | `AGGRESSIVE` | 공격투자형 |

> 근거: 손실감수 의향(문서 40) + 손실감내 능력(35) + 투자기간·유동성(15) + 투자경험·지식(10) rubric 을 현재 7문항 범위로 축약. 정식 A·B·C·R·G 모델(소득·부채·비상자금 비중·금융지식 등)은 목표/추천 도메인에서 확장.

## 확정된 사항

- **휴대폰 인증 없음** — 번호 중복만 아니면 가입 (`DUPLICATE_PHONE` 체크만)
- **약관 동의** — 별도 단계 없이 회원가입 요청에 포함 (`agreements`)
- **소셜 로그인 유지** — 카카오/네이버
- **성별(`gender` MALE/FEMALE)을 회원가입에서 필수로 받음** — 커플 연결 시 `male_id`/`female_id` 배정, 동성 연결 거부
- **초대 코드** — 회원가입 시 자동 발급 (`invitation` 마스터 row), 6자리 영숫자, **만료 없음**
- **커플 연결은 양방향** — 두 사람이 서로 코드 입력, 또는 요청 → 수락. `couple` 레코드는 첫 요청 시 `WAIT`로 생성, 연결 시 `CONNECTED`
- **동시에 여러 명에게 요청/수신 가능** → `GET /couples/status`는 배열 반환
- **알림 미구현** — 상대는 `GET /couples/status`로 들어온 요청 확인

## ERD 반영 필요

- `user.gender VARCHAR(10)` 추가 (`MALE` / `FEMALE`) — 기존 `role`은 권한용으로 분리
- `couple`: `groom_id`/`bride_id` → **`male_id`/`female_id`**, `UNIQUE(male_id, female_id)` 추가
- `invitation` 테이블 **유지** (durimoa 방식: 마스터 row + 요청자별 복사 row). 상태값 `CREATED` / `REQUESTED` / `ACCEPTED`, `UNIQUE(inviter_id, invitee_id)`. **`expired_at` 제거**
- `couple.connected_at DATETIME` 추가 (선택 — durimoa엔 없음)

## 미확정 (개발 착수 전 결정)

- **세션 유지**: 현재 access token 만 발급 (만료 시 재로그인). refresh token + Redis 기반 재발급(`POST /auth/token/refresh`)은 추후 추가 예정.
- **투자성향 점수 배점표** — "투자성향 점수 산출" 섹션은 MVP 초기 가설. 팀 튜닝 필요 (`RiskProfileCalculator` 상수).
- **A·B·C·R·G 위험 모델** — 정식 모델(개인 허용상한, 서비스 안전상한, 공동자금 설문 C, 목표 요구점수 G, AI 권장범위 R)은 온보딩 범위 밖. 목표/추천 도메인에서 설계.
- `jointRiskProfileType` 산출 로직 (= 위 모델의 R. 온보딩 슬라이스에서는 다루지 않음)
- 연결 해제 시 `goal` / `investment_report` 처리
- `currentAmount` 자동 집계(계좌 연동) 여부
- `maxTolerableLossRate` / `maxAllowedLossRate` 구간 선택 vs 자유 숫자
