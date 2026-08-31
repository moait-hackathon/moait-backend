# common/response

API 공통 응답 규격.

- `ApiResponse<T>` — 성공 응답 래퍼 (`success` / `message` / `data`)
- `ErrorResponse` — 실패 응답 래퍼 (`code` / `message` / `errors`)

Controller 반환 타입은 `ResponseEntity<ApiResponse<T>>` 를 유지한다.
