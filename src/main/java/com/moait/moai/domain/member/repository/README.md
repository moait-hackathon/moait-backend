# domain/member/repository

DB 접근 전용. `JpaRepository<Entity, ID>` 상속 인터페이스.

- `MemberRepository extends JpaRepository<Member, Long>`
- 파생 쿼리 컨벤션: `findByEmail`, `existsByEmail`, `countBy...`
- 복잡한 쿼리는 `@Query`(JPQL)
