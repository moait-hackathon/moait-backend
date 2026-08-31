# domain/member/controller

`@RestController`. HTTP 요청 수신 · `@Valid` 검증 · Service 호출 · 응답 래핑만 담당.

- `MemberController` — `/api/members`
- springdoc 어노테이션(`@Tag`, `@Operation`) 필수
- 비즈니스 로직 / DB 접근 금지
