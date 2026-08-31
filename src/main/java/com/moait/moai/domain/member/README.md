# domain/member

회원 도메인. **다른 도메인이 따라야 할 계층 틀 예시**로만 존재하며, 실제 필드/로직은 도메인 설계 확정 후 채운다.

계층 흐름:

```
controller → service (interface) → serviceImpl → repository → entity
                                                     ↑
                                                    dto (요청/응답 변환)
```

새 도메인은 `domain/` 아래에 이 폴더와 동일한 하위 구조로 추가한다.
