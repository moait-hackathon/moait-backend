# 투자 분석 API 로컬 테스트

새 설문 형식·계산 규칙·임시 G 모델의 한계는 [계산 규칙](investment-agreement-rules.md)을 참고합니다.

## 1. DB 준비

MySQL에서 저장소 루트의 `MoAItDB.sql`을 먼저 실행하고, 다음 파일을 실행합니다.

```text
docs/sample-investment-data.sql
```

샘플 데이터의 주요 ID는 다음과 같습니다.

| 데이터 | ID |
| --- | --- |
| 개인 A 사용자 | `990001` |
| 개인 B 사용자 | `990002` |
| 커플 | `990001` |
| 공동목표 | `990001` |

## 2. OpenAI 없이 실행

기본 설정에서는 Spring AI 채팅 모델이 비활성화됩니다. 점수 계산과 기본 합의안은 OpenAI 키 없이 실행됩니다.

```powershell
$env:SPRING_PROFILES_ACTIVE="local"
.\gradlew.bat bootRun
```

## 3. OpenAI 합의안 생성 활성화

API 키는 서버 환경변수로만 설정합니다.

```powershell
$env:OPENAI_API_KEY="발급받은_API_키"
$env:SPRING_AI_MODEL_CHAT="openai"
$env:OPENAI_MODEL="gpt-4o-mini"
.\gradlew.bat bootRun
```

키를 소스나 `application.properties`에 직접 기록하지 않습니다.

## 4. 요청 전송

분석 API는 스펙상 JWT 토큰 인증이 필요한 API입니다. (단, 개발 테스트 편의를 위해 `SecurityConfig`에 의해 무인증 접근이 허용되어 있으므로 토큰 없이도 호출할 수 있습니다.) 샘플 JSON의 `goalId`는 실제 존재하는 공동 목표 ID여야 합니다.

```powershell
Invoke-RestMethod `
    -Method Post `
    -Uri "http://localhost:8080/api/v1/investment-analyses/agreements" `
    -ContentType "application/json; charset=utf-8" `
    -InFile ".\docs\investment-agreement-request.json"
```

Swagger UI에서도 같은 요청을 보낼 수 있습니다.

```text
http://localhost:8080/swagger-ui.html
```

## 현재 DB 연동 범위

샘플 SQL은 기존 `user`, `couple`, `investment_profile`, `goal`,
`investment_account`, `investment_asset` 구조를 검증할 수 있도록 구성했습니다.

현재 분석 API는 DB의 ID를 받아 자동 조회하지 않고, 계산에 필요한 전체 설문과 목표 데이터를 요청 JSON으로 받습니다. 기존 DB에는 비상자금 개월 수, 고정비 부담률, 원금보전 기준, 금융지식 점수와 같은 필수 값이 없기 때문입니다. DB 자동 조회 API를 만들려면 해당 입력을 저장할 스키마를 먼저 확장해야 합니다.

분석 결과는 요청의 `goalId`로 기존 `investment_report`에 저장됩니다. 로그인 여부를 구분하지 않습니다. 기존 DB에는 `docs/investment-report-agreement-migration.sql`을 한 번 적용합니다.

`targetDate`는 실행 시점보다 1개월 이상 100년 이내여야 합니다. 샘플 날짜가 지난 경우 미래 날짜로 변경합니다.
