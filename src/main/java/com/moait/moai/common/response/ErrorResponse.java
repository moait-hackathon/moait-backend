package com.moait.moai.common.response;

import com.moait.moai.common.exception.ErrorCode;
import lombok.Getter;

/**
 * 실패 응답 공통 래퍼.
 *
 * <pre>
 * { "success": false, "errorCode": "...", "message": "..." }
 * </pre>
 *
 * 성공 응답은 {@link ApiResponse} 를 사용한다. 검증 실패 시 {@code message} 에는 첫 번째 필드 에러 메시지를 담는다.
 */
@Getter
public class ErrorResponse {

    private final boolean success = false;
    private final String errorCode;
    private final String message;

    private ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message);
    }
}
