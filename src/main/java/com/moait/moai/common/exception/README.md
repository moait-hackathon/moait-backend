# common/exception

전역 예외 처리.

- `ErrorCode` — 서비스 전역 에러 코드 enum (HttpStatus + 메시지)
- `BusinessException` — 비즈니스 규칙 위반 최상위 예외. 도메인 예외가 상속
- `GlobalExceptionHandler` — `@RestControllerAdvice`, 모든 에러를 `ErrorResponse` 규격으로 변환
