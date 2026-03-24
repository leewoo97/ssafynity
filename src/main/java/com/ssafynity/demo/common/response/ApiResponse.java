package com.ssafynity.demo.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 모든 REST API 응답의 공통 래퍼.
 * <pre>
 * 성공: { "success": true,  "data": {...} }
 * 실패: { "success": false, "error": { "code": "...", "message": "..." } }
 * </pre>
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private T data;
    private ErrorDetail error;

    // ── 성공 ──────────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        r.data = data;
        return r;
    }

    public static <T> ApiResponse<T> ok() {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = true;
        return r;
    }

    // ── 실패 ──────────────────────────────────────────────────────────────────

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> r = new ApiResponse<>();
        r.success = false;
        r.error = new ErrorDetail(code, message);
        return r;
    }

    @Getter
    public static class ErrorDetail {
        private final String code;
        private final String message;

        ErrorDetail(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}
