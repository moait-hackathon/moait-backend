# domain/member/dto

요청/응답 전용 객체. Entity 를 직접 노출하지 않는다.

- `MemberCreateRequestDTO` — 요청 (`@NotBlank`, `@Email` 등 검증)
- `MemberResponseDTO` — 응답 (`from(Member)` 정적 팩토리)
- Request / Response 를 명확히 분리
