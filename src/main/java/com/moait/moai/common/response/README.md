# common/response

API 공통 응답 규격.

- `ApiResponse<T>` — 성공 응답 래퍼 (`success` / `message` / `data`)
- `ErrorResponse` — 실패 응답 래퍼 (`success=false` / `errorCode` / `message`). 검증 실패 시 `message` 는 첫 번째 필드 에러 메시지.

Controller 반환 타입은 `ResponseEntity<ApiResponse<T>>` 를 유지한다.
