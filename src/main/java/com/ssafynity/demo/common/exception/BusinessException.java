package com.ssafynity.demo.common.exception;

import lombok.Getter;

/**
 * 서비스 레이어에서 발생하는 비즈니스 예외.
 * 컨트롤러까지 전파되어 GlobalExceptionHandler 가 처리한다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
