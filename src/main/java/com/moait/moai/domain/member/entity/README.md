# domain/member/entity

JPA 엔티티. DB 테이블과 1:1 매핑.

- `Member` — `BaseTimeEntity` 상속
- `@Getter` 만, `@Setter`/`@Data` 금지
- 기본 생성자 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`
- 상태 변경은 의미 있는 도메인 메서드로
- Controller 외부로 직접 노출 금지
