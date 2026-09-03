# domain

도메인별 패키지. 각 도메인은 아래 계층 흐름을 따른다.

```
controller → service (interface) → serviceImpl → repository → entity
                                                     ↑
                                                    dto (요청/응답 변환)
```

하위 구조: `controller/` `service/` `repository/` `entity/` `dto/`

## 도메인 목록

| 패키지 | 범위 | 주요 엔드포인트 |
|---|---|---|
| `auth` | 회원가입 · 로그인 · 토큰 발급 | `POST /api/auth/**` |
| `user` | 프로필 · 기본 재무정보 · 투자성향 설문(`InvestmentProfile`) | `/api/onboarding/**`, `/api/users/**` |
| `couple` | 초대 코드(`Invitation`) · 커플 연결/해제 | `/api/couples/**` |
| `goal` | 부부 공동 목표 | `/api/goals/**` |
| `report` | AI 투자 추천(`InvestmentReport`) | `/api/goals/me/report` (예정) |

- 엔티티는 `common/entity/BaseCreatedEntity` 또는 `BaseTimeEntity` 를 상속한다.
- API 규격은 저장소 루트 `docs/api-spec.md`, 스키마는 `MoAItDB.sql` 참고.
