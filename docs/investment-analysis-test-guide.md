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

분석 API는 현재 개발 테스트용 `SecurityConfig`에 의해 무인증 접근이 허용됩니다. 요청은 연결된 커플 구성원의 `userId` 하나이며, 나머지는 DB에서 조회합니다.

```json
{"userId": 990001}
```

`990002`로 호출해도 같은 커플을 분석합니다. A는 male_id, B는 female_id로 고정됩니다.

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

샘플 SQL은 최신 `user`, `couple`, `goal`, `investment_account`, `investment_asset` 구조를 사용합니다. 폐기된 `investment_profile`은 사용하지 않습니다.

개인 A·B는 활성 자산의 평가금액 비중과 위험등급으로 계산하고, C는 goal의 공동 설문으로 계산합니다. 샘플을 막 적재했을 때 A=84, B=13, C=61, 가중점수=56, 최종상한 및 R=45입니다. 기간 점수는 현재 날짜와 저장된 목표일에 따라 달라질 수 있습니다.

기존 `request-*.json`, `suitable-request.json`도 userId 형식으로 갱신되어 모두 같은 요청입니다. 파일명으로 판정이 정해지지 않습니다. 다른 시나리오는 테스트 DB의 목표금액·월 투자금·목표일 또는 보유 자산을 변경한 뒤 같은 요청으로 확인합니다.

분석 결과는 조회한 목표 ID로 `investment_report`에 새로 저장됩니다. 기존 목표는 변경하지 않습니다. 실제 DB 스키마는 현재 `MoAItDB.sql`의 공동 설문 컬럼과 보고서 컬럼을 포함해야 합니다. 이번 코드 수정에서 실제 DB에 SQL을 실행하지는 않았습니다.

`goal.target_date`는 실행 시점보다 1개월 이상 100년 이내여야 합니다. 목표가 DRAFT이거나 공동 설문이 누락되거나 한 사람의 양수 활성 자산이 없으면 422이며 응답 메시지에 보완할 데이터를 안내합니다. 미분류 ETF·펀드·연금·기타 자산에는 위험등급을 등록해야 하고, 양수 외화 자산은 현재 환율 환산이 없어 지원하지 않습니다.

자동 검증은 `.\gradlew.bat test`로 실행합니다. 개인 점수·상한·보고서 저장은 단위 테스트, 목표·자산 조회는 H2 통합 테스트, userId 입력은 MockMvc 테스트로 확인합니다.
