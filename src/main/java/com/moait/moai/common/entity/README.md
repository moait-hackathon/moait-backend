# common/entity

엔티티 공통 상위 클래스 (`@MappedSuperclass`, JPA Auditing).

- `BaseCreatedEntity` — `createdAt` 만 자동 관리. `updated_at` 컬럼이 없는 테이블용
- `BaseTimeEntity` — `BaseCreatedEntity` + `updatedAt`. `updated_at` 컬럼이 있는 테이블용 (예: `goal`)

Auditing 활성화는 `common/config/JpaConfig` 의 `@EnableJpaAuditing`.
