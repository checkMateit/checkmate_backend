package com.checkit.studyservice.exception;

import com.checkit.common.dto.ApiResponse;
import com.checkit.common.dto.ErrorResponse;
import com.checkit.common.exception.BusinessException;
import com.checkit.common.exception.CommonCode;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        CommonCode code = e.getCode();
        String msg = (e.getDetail() != null) ? (code.getMessage() + " - " + e.getDetail()) : code.getMessage();
        return ResponseEntity.status(code.getHttpStatus()).body(ApiResponse.fail(code, msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .orElse(CommonCode.BAD_REQUEST.getMessage());
        return ResponseEntity.status(CommonCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.fail(CommonCode.BAD_REQUEST, msg));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraint(ConstraintViolationException e) {
        return ResponseEntity.status(CommonCode.BAD_REQUEST.getHttpStatus())
                .body(ApiResponse.fail(CommonCode.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleOther(Exception e) {
        ErrorResponse er = ErrorResponse.of(CommonCode.INTERNAL_SERVER_ERROR.getCode(), CommonCode.INTERNAL_SERVER_ERROR.getMessage(), "-");
        return ResponseEntity.status(CommonCode.INTERNAL_SERVER_ERROR.getHttpStatus()).body(ApiResponse.failure(er));
    }
}
