package com.moait.moai.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 서비스 전역 에러 코드.
 *
 * <p>도메인별 에러 코드는 해당 도메인 작업 시 이 enum 에 추가한다.
 */
@Getter
public enum ErrorCode {

    // ===== 공통 =====
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "요청 값이 올바르지 않습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "대상을 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "지원하지 않는 요청 방식입니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),

    // ===== 인증 / 회원가입 =====
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 가입된 휴대폰 번호입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    TERMS_REQUIRED_NOT_AGREED(HttpStatus.UNPROCESSABLE_CONTENT, "필수 약관에 동의해야 합니다."),
    SOCIAL_AUTH_FAILED(HttpStatus.BAD_REQUEST, "소셜 인증에 실패했습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),

    // ===== 커플 연결 =====
    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 초대 코드입니다."),
    CANNOT_CONNECT_SELF(HttpStatus.CONFLICT, "본인의 초대 코드로는 연결할 수 없습니다."),
    SAME_GENDER(HttpStatus.CONFLICT, "성별이 남/녀로 달라야 연결할 수 있습니다."),
    ALREADY_CONNECTED(HttpStatus.CONFLICT, "나 또는 상대가 이미 다른 사람과 연결되어 있습니다."),
    REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 상대의 대기 중인 연결 요청이 없습니다."),
    COUPLE_NOT_FOUND(HttpStatus.NOT_FOUND, "연결된 커플이 없습니다."),
    NOT_CONNECTED(HttpStatus.CONFLICT, "연결된(CONNECTED) 상태가 아닙니다.");

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }
}
