# AGENTS.md

이 문서는 **MoAIt** 팀의 **MoAI** 백엔드 프로젝트 개발에 참여하는 AI 에이전트 및 개발자를 위한 지침서입니다. 프로젝트의 구조, 컨벤션, 개발 규칙 및 프로세스를 정의합니다.

---

## 프로젝트 개요

### 프로젝트 이름
- **MoAI - 백엔드 (Backend)**
- 팀: **MoAIt**
- 예비부부를 위한 자산 관리 서비스 백엔드 API 서버

### 기술 스택
- **Language**: Java 21 (Gradle Toolchain 으로 고정, foojay-resolver 가 자동 프로비저닝)
- **Framework**: Spring Boot 4.1.1 (Spring Framework 7 / Spring MVC), Spring Security 7
- **ORM / Persistence**: Spring Data JPA (Hibernate), HikariCP (Spring Boot 기본 커넥션 풀)
- **Database**: MySQL 8.x (`com.mysql:mysql-connector-j`). 스키마는 저장소 루트 `MoAItDB.sql` 로 관리 (`ddl-auto=validate`)
- **Build Tool**: Gradle (Wrapper 9.7.1), 산출물은 실행 가능한 `bootJar` (내장 Tomcat)
- **API Documentation**: springdoc-openapi 3.1.0 (`springdoc-openapi-starter-webmvc-ui`)
- **인증**: JWT (JJWT `io.jsonwebtoken:jjwt-*:0.12.6`) — STATELESS. `common/security` 참고
- **Validation**: Jakarta Bean Validation (`spring-boot-starter-validation`)
- **Logging**: SLF4J + Logback (Spring Boot 기본), Lombok `@Slf4j`
- **Utilities**: Lombok, Jackson (JSR-310 자동 구성), Spring Boot DevTools
- **Testing**: JUnit 5, Mockito, Spring Boot Test 슬라이스(`@WebMvcTest`, `@DataJpaTest`), `spring-security-test`, 테스트 DB 는 H2(`MODE=MySQL`)

> Spring Boot 4 는 **Jackson 3** (`tools.jackson.databind.*`) 를 사용한다. 자동 구성되는 `ObjectMapper` 빈은 Jackson 3 타입이므로, 직접 주입 시 `com.fasterxml.jackson.databind.ObjectMapper` 가 아니라 `tools.jackson.databind.ObjectMapper` 를 import 한다.

> 아직 도입하지 않았지만 논의 중인 항목: Redis(캐시/세션), DB 마이그레이션 도구(Flyway). 도입 시 이 문서를 갱신합니다.

### 코드 작성 규칙
1. **Google/Oracle Java Code Style 준수**:
   - 들여쓰기는 **4 Space** 를 사용하며 탭(Tab) 사용을 금지합니다. (`.editorconfig` 로 강제)
   - 모든 파일은 **UTF-8** 인코딩을 적용합니다.
2. **의존성 주입 (Dependency Injection)**:
   - 필드 주입(`@Autowired`) 대신 `@RequiredArgsConstructor` 를 활용한 **생성자 주입** 을 사용합니다.
3. **계층형 아키텍처 (Layered Architecture)**:
   - `Controller` ➔ `Service Interface` ➔ `Service Implementation` ➔ `Repository` ➔ `Entity` 흐름을 엄격히 준수합니다.
4. **예외 처리 (Exception Handling)**:
   - 비즈니스 예외는 커스텀 Exception(예: `MemberNotFoundException`, `InvalidInputException`)을 정의하고 `@RestControllerAdvice` 에서 일괄 처리합니다.
   - 에러 코드는 `ErrorCode` enum 으로 관리하고, 공통 `ErrorResponse` 규격으로 응답합니다.
5. **JPA / 영속성 규칙**:
   - Entity 를 Controller 밖(요청/응답)으로 직접 노출하지 않습니다. 항상 DTO 로 변환합니다.
   - 연관관계 `fetch` 전략은 기본 `LAZY`. 필요한 조회는 fetch join / `@EntityGraph` / 프로젝션으로 **N+1 을 방지**합니다.
   - 조회 전용 메서드에는 `@Transactional(readOnly = true)` 를 지정합니다.
   - Entity 에는 `@Setter` 를 열지 않고, 의미 있는 도메인 메서드로 상태를 변경합니다. 기본 생성자는 `protected`.
   - 모든 환경에서 `spring.jpa.hibernate.ddl-auto` 는 `validate`(로컬/운영) 또는 `none` 을 사용합니다. **스키마의 소스 오브 트루스는 저장소 루트 `MoAItDB.sql`** 이며, 변경 시 이 파일을 수정합니다 (엔티티가 아님). ERDCloud 다이어그램은 시각화 용도이며 SQL 과 어긋나면 SQL 이 우선입니다.
   - 로그 출력 시 개인정보(비밀번호, 주민번호) 및 계좌번호가 직접 노출되지 않도록 가공 처리합니다.

### 네이밍 규칙

#### 1. Java 클래스 및 인터페이스 (PascalCase)
- **Controller**: `[Domain]Controller` (예: `MemberController`, `AccountController`)
- **Service Interface**: `[Domain]Service` (예: `MemberService`)
- **Service Implementation**: `[Domain]ServiceImpl` (예: `MemberServiceImpl`)
- **Repository**: `[Domain]Repository` (예: `MemberRepository`)
- **Entity**: `[Domain]` (예: `Member`, `Account`)
- **Request DTO**: `[Domain][Action]RequestDTO` (예: `MemberCreateRequestDTO`, `BudgetUpdateRequestDTO`)
- **Response DTO**: `[Domain][Action]ResponseDTO` 또는 `[Domain]ResponseDTO` (예: `MemberResponseDTO`, `AccountDetailResponseDTO`)
- **Exception**: `[Domain][Reason]Exception` (예: `MemberNotFoundException`, `BudgetExceededException`)

#### 2. 메서드 및 변수 (camelCase)
- **Java 메서드/변수**: camelCase (예: `findMemberById`, `totalAmount`)
- **Spring Data JPA 쿼리 메서드**: Spring Data 파생 쿼리 컨벤션을 따릅니다.
   - 조회: `findByEmail`, `findById`, `findAllByCoupleId`
   - 존재 여부: `existsByEmail`
   - 개수 조회: `countByCoupleId`
   - 삭제: `deleteById`, `deleteByMemberId`
   - 복잡한 쿼리는 `@Query`(JPQL) 사용, 동적 쿼리는 별도 논의(QueryDSL 등)

#### 3. 기타 규칙
- **상수 (Constant)**: UPPER_SNAKE_CASE (예: `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`)
- **DB 테이블/컬럼**: lower_snake_case (예: `member_id`, `created_at`) — `@Column`, `@Table` 로 명시 매핑
- **URL Endpoint**: lower-kebab-case 및 복수형 명사 사용 (예: `/api/members`, `/api/budgets/monthly`)

### 우선 순위
1. **기능 동작의 정확성 및 비즈니스 데이터 검증**: 금융/자산 데이터를 다루므로 데이터 정확성, 보안 및 트랜잭션 처리 최우선.
2. **코드 가독성 및 아키텍처 규칙 준수**: 도메인별 계층 분리 및 DTO 객체 분리 엄수.
3. **DB 쿼리 성능 및 리소스 최적화**: 인덱스 타는 쿼리 작성, N+1 방지, (도입 시) 캐싱 적재적소 활용.
4. **문서화 및 예외 처리 고도화**: Swagger 명세 최신화 및 사용자 친화적 공통 응답/에러 포맷 유지.

### 폴더 구조
> `common/` 공통 인프라는 구현 완료. `domain/` 은 도메인별로 순차 구현 중이며, 계층 흐름과 도메인 목록은 `domain/README.md` 참고.
> 새 도메인은 `domain/<name>/` 아래에 `controller/ service/ repository/ entity/ dto/` 하위 패키지 구조로 추가합니다.

```text
moait-backend/
├── MoAItDB.sql                                      # DB 스키마 (소스 오브 트루스)
├── docs/                                            # api-spec.md, erd-user-couple-goal.md, DEPLOY.md
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── moait/
│   │   │           └── moai/
│   │   │               ├── MoaiApplication.java     # 메인 클래스
│   │   │               ├── common/                  # 공통 인프라
│   │   │               │   ├── config/              # SecurityConfig, JpaConfig
│   │   │               │   ├── security/            # JwtTokenProvider, JwtAuthenticationFilter, EntryPoint, JwtProperties
│   │   │               │   ├── entity/              # BaseCreatedEntity, BaseTimeEntity (Auditing 공통 상위)
│   │   │               │   ├── exception/           # ErrorCode, BusinessException, GlobalExceptionHandler
│   │   │               │   ├── response/            # ApiResponse, ErrorResponse
│   │   │               │   └── util/                # MaskingUtils 등 유틸 클래스
│   │   │               └── domain/                  # 비즈니스 도메인 (auth / user / couple / goal / report)
│   │   │                   └── <name>/
│   │   │                       ├── controller/
│   │   │                       ├── service/         # 인터페이스 + ServiceImpl
│   │   │                       ├── repository/
│   │   │                       ├── entity/
│   │   │                       └── dto/
│   │   └── resources/
│   │       ├── application.properties               # 공통 설정 (jwt 개발 기본값 포함)
│   │       ├── application-local.properties         # 로컬 전용 설정 (git 제외)
│   │       └── application-prod.properties          # 운영 설정 (모든 시크릿은 환경변수 주입)
│   └── test/
│       ├── java/                                    # JUnit5 및 Mockito 테스트 코드
│       └── resources/application.properties         # 테스트용 (H2 + jwt 고정값)
├── build.gradle                                     # 의존성 및 빌드 설정
├── settings.gradle                                  # foojay-resolver (JDK 자동 프로비저닝)
└── AGENTS.md                                        # 에이전트 및 개발 지침서
```

---

## 규칙

### 타입 규칙
1. **DTO 필드 타입**:
   - Java Primitive Type(`int`, `long`, `boolean`) 사용을 지양하고, **Wrapper Class**(`Integer`, `Long`, `Boolean`)를 사용하여 Null 처리 및 데이터 부재 상태 표현을 유연하게 합니다.
2. **금액/수량 처리**:
   - 금액 관련 데이터는 오차 방지를 위해 `Long` 또는 `BigDecimal` 타입을 사용하며, 소숫점 연산이 수반될 시 `BigDecimal` 을 필수 적용합니다.
3. **날짜 및 시간 타입**:
   - `java.util.Date` 나 `java.sql.Timestamp` 사용을 금지하고, `java.time.LocalDate`, `java.time.LocalDateTime` 을 사용합니다.
   - JSON 직렬화/역직렬화 시 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")` 어노테이션을 지정합니다.
   - 생성/수정 시각은 JPA Auditing(`@CreatedDate`, `@LastModifiedDate`) 으로 자동 관리합니다.
4. **공통 응답 Wrapper 타입**:
   - Controller 의 반환 타입은 `ResponseEntity<ApiResponse<T>>` 형태를 유지하여 성공/실패 여부, 상태 코드, 메세지, 데이터를 통일된 규격으로 제공합니다.

### 컴포넌트 규칙
1. **Controller**:
   - HTTP 요청 수신, `@Valid` 를 통한 유입 파라미터 검증, Service 레이어 호출, 응답 래핑 역할만 수행합니다.
   - Controller 에 비즈니스 로직이나 DB 접근 코드가 포함되어서는 안 됩니다.
   - 모든 엔드포인트에 springdoc 어노테이션(`@Tag`, `@Operation`, `@ApiResponse`)을 작성합니다.
2. **Service**:
   - 비즈니스 핵심 로직 처리, 트랜잭션 관리(`@Transactional`), 도메인 간 조율을 담당합니다.
   - Service 인터페이스와 구현체(`ServiceImpl`)를 분리하여 유연성을 확보합니다.
3. **Repository (DAO)**:
   - DB 접근 전용 레이어로 `JpaRepository<Entity, ID>` 를 상속한 인터페이스 형태를 가집니다.
   - 커스텀 구현이 필요하면 `[Domain]RepositoryCustom` + `[Domain]RepositoryImpl` 패턴을 사용합니다.
4. **Entity**:
   - DB 테이블과 1:1 매핑되는 영속 객체. Controller 외부로 직접 노출 금지.
   - `@Getter` 만 사용하고 `@Setter`/`@Data` 금지. 기본 생성자는 `@NoArgsConstructor(access = AccessLevel.PROTECTED)`.
5. **DTO (Data Transfer Object)**:
   - View/API 요청 및 응답에 사용하는 객체로 RequestDTO 와 ResponseDTO 를 명확히 분리합니다.
   - Entity ➔ DTO 변환은 DTO 의 정적 팩토리 메서드(`from`, `of`) 또는 별도 Mapper 에서 수행합니다.

### 금지 규칙
1. **Controller 내 비즈니스 로직 작성 금지**: 서비스 레이어를 거치지 않는 Repository 직접 호출이나 로직 연산 금지.
2. **Entity 를 API 요청/응답 타입으로 사용 금지**: 반드시 DTO 로 변환.
3. **N+1 유발 코드 방치 금지**: 반복문 내 연관 엔티티 접근 시 fetch join / `@EntityGraph` / 배치 사이즈로 해결.
4. **`spring.jpa.hibernate.ddl-auto=create/update` 를 공유·운영 환경에 사용 금지**.
5. **민감 정보 로그 출력 금지**: 비밀번호, JWT 토큰, 계좌 번호, 주민등록번호 등을 `log.info()` 나 에러 메시지에 노출 금지.
6. **`System.out.println()` 사용 금지**: 반드시 SLF4J (`log.info()`, `log.error()`, `log.debug()`) 를 사용. (`@Slf4j`)
7. **예외 무시 (Exception Swallowing) 금지**: `catch (Exception e) {}` 와 같이 빈 catch 블록으로 에러를 묵인하는 코드 금지.
8. **`main` / `develop` 브랜치 직접 push 금지**: 반드시 작업 브랜치에서 Pull Request(PR)를 통해 코드 리뷰 후 머지.
9. **시크릿 하드코딩 금지**: DB 비밀번호, JWT 시크릿 등은 `application-local.properties` 또는 환경변수로 주입. 커밋 금지.

### 커밋 규칙
- **Conventional Commits** 형식을 준수합니다.

```text
<type>: <description> (#issue_number)
```

#### Prefix (Type)
- `feat`: 새로운 기능 추가
- `fix`: 버그 수정
- `docs`: 문서 수정 (README.md, AGENTS.md 등)
- `style`: 코드 포맷팅, 세미콜론 누락 등 (코드 동작 변경 없음)
- `refactor`: 코드 리팩토링 (기능 추가 및 버그 수정 없음)
- `test`: 테스트 코드 추가 및 수정
- `chore`: 빌드 업무, 패키지 매니저, 기타 환경 설정 변경

#### 작성 예시
- `feat: 회원 가입 및 비밀번호 암호화 기능 구현 (#12)`
- `fix: 공동 예산 조회 시 NullPointerException 예외 수정 (#45)`
- `docs: AGENTS.md 지침서 문서 업데이트`

### git 규칙
1. **브랜치 전략 (Git Flow)**:
   - `main`: 운영 배포용 브랜치
   - `develop`: 개발 통합 브랜치
   - `feat/[기능명]`: 신규 기능 개발 브랜치 (예: `feat/member-login`, `feat/budget-calc`)
   - `fix/[버그명]`: 버그 수정 브랜치 (예: `fix/jwt-expiration`)
   - `docs/[문서명]`: 문서 작업 브랜치
2. **Pull Request (PR) 프로세스**:
   - PR 템플릿 양식을 준수하여 PR 내용 작성.
   - 연관된 Issue 번호를 명시 (`Resolves #이슈번호`).
   - 최소 1명 이상의 팀원 승인(Approve) 후 `develop` 브랜치로 Squash and Merge 진행.

### 빌드 및 실행 명령어
| 목적 | 명령어 |
| --- | --- |
| 전체 빌드 | `./gradlew build` |
| 컴파일만 | `./gradlew compileJava` |
| 전체 테스트 | `./gradlew test` |
| 단일 테스트 | `./gradlew test --tests "com.moait.moai.domain.auth.AuthServiceTest"` |
| 앱 실행 | `./gradlew bootRun` |
| 산출물(jar) | `./gradlew bootJar` → `build/libs/moai-0.0.1-SNAPSHOT.jar` |
| Swagger UI | 앱 실행 후 `http://localhost:8080/swagger-ui.html` |

> 로컬 실행에는 MySQL 접속 정보가 필요합니다 (`application-local.properties`). 최초 1회 `MoAItDB.sql` 로 스키마를 생성합니다. `./gradlew test` 는 H2 인메모리로 동작하므로 MySQL 불필요.

### 작업 분류
AI 에이전트 및 개발자는 작업을 진행하기 전 다음 분류에 따라 작업을 명확히 식별합니다.

1. **기능 개발 (Feature Task)**: Entity 작성 ➔ Repository 인터페이스 작성 ➔ DTO 생성 ➔ Service 인터페이스/구현 ➔ Controller 구현 ➔ Swagger 명세 작성 ➔ 테스트 코드 추가
2. **버그 수정 (Bugfix Task)**: 오류 로그 및 재현 조건 확인 ➔ 원인 분석 및 범위 파악 ➔ 수정 및 테스트 작성 ➔ 회귀 테스트 검증
3. **리팩토링 (Refactoring Task)**: 기존 테스트 Pass 확인 ➔ 코드 구조 개선/성능 최적화 ➔ 기존 기능 영향도 테스트 재검증
4. **문서화 및 설정 (Docs/Chore Task)**: 설정 파일 수정 또는 API 명세/가이드 문서 업데이트 및 검수

### 작업 완료 보고
작업이 완료되었을 때 AI 에이전트는 작성/수정한 내용과 검증 결과를 다음 항목에 맞춰 사용자에게 체계적으로 보고해야 합니다.

1. **작업 요약 (Summary)**:
   - 수행한 주요 작업 내용을 명확하게 정리
2. **변경 파일 목록 (Changed Files)**:
   - 생성/수정/삭제된 모든 파일의 경로를 clickable link 로 제공
3. **검증 결과 (Verification Results)**:
   - `./gradlew test` 또는 실행을 통해 검증한 결과 및 테스트 통과 여부 명시
4. **주의사항 및 향후 작업 (Notes & Next Steps)**:
   - 리뷰어가 주의 깊게 봐야 할 사항이나 후속 완료해야 할 작업(TODO) 정리
