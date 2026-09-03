package com.moait.moai.common.response;

import com.moait.moai.common.exception.ErrorCode;
import java.util.List;
import lombok.Getter;
import org.springframework.validation.BindingResult;

/**
 * 실패 응답 공통 래퍼.
 *
 * <pre>
 * { "code": "...", "message": "...", "errors": [ { "field": "...", "reason": "..." } ] }
 * </pre>
 *
 * {@code errors} 는 필드별 검증 실패 목록. 없으면 빈 배열.
 */
@Getter
public class ErrorResponse {

    private final String code;
    private final String message;
    private final List<FieldError> errors;

    private ErrorResponse(String code, String message, List<FieldError> errors) {
        this.code = code;
        this.message = message;
        this.errors = errors;
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.name(), message, List.of());
    }

    public static ErrorResponse of(ErrorCode errorCode, BindingResult bindingResult) {
        return new ErrorResponse(errorCode.name(), errorCode.getMessage(), FieldError.from(bindingResult));
    }

    @Getter
    public static class FieldError {

        private final String field;
        private final String reason;

        private FieldError(String field, String reason) {
            this.field = field;
            this.reason = reason;
        }

        private static List<FieldError> from(BindingResult bindingResult) {
            return bindingResult.getFieldErrors().stream()
                    .map(error -> new FieldError(
                            error.getField(),
                            error.getDefaultMessage() != null ? error.getDefaultMessage() : "유효하지 않은 값입니다."))
                    .toList();
        }
    }
}
