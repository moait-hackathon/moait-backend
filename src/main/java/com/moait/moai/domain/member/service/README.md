# domain/member/service

비즈니스 로직 · 트랜잭션(`@Transactional`) · 도메인 조율.

- `MemberService` — 인터페이스
- `MemberServiceImpl` — 구현체 (`@Service`, 생성자 주입)
- 조회 전용 메서드는 `@Transactional(readOnly = true)`
