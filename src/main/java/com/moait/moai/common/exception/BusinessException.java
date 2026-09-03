package com.moait.moai.common.exception;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 최상위 예외. 도메인 예외가 상속한다.
 * {@link GlobalExceptionHandler} 에서 {@link com.moait.moai.common.response.ErrorResponse} 규격으로 변환된다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final transient ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
