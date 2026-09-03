package com.moait.moai.common.response;

import lombok.Getter;

/**
 * 성공 응답 공통 래퍼.
 *
 * <pre>
 * { "success": true, "message": "...", "data": { ... } }
 * </pre>
 *
 * 실패 응답은 {@link ErrorResponse} 를 사용한다.
 */
@Getter
public class ApiResponse<T> {

    private static final String DEFAULT_MESSAGE = "요청이 처리되었습니다.";

    private final boolean success;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, DEFAULT_MESSAGE, data);
    }

    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
}
