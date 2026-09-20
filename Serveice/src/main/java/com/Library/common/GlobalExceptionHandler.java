package com.Library.common;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 全局异常
@Slf4j
@RestControllerAdvice //将 Controller 层所有异常通过这里进行拦截
public class GlobalExceptionHandler {

    // "请求体校验失败异常" 和 "参数绑定校验失败异常" 的处理
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<DataInfo<Void>> handleValidation(Exception ex) {
        String msg = "参数校验失败";
        if (ex instanceof MethodArgumentNotValidException manv && manv.getBindingResult().getFieldError() != null) {
            msg = manv.getBindingResult().getFieldError().getDefaultMessage();
        }
        if (ex instanceof BindException be && be.getBindingResult().getFieldError() != null) {
            msg = be.getBindingResult().getFieldError().getDefaultMessage();
        }
        return ResponseEntity.badRequest().body(DataInfo.fail(msg));
    }

    // 非法参数异常
    @ExceptionHandler(IllegalAccessException.class)
    public ResponseEntity<DataInfo<Void>> handlerBadRequest(IllegalAccessException ex) {
        return ResponseEntity.badRequest().body(DataInfo.fail(ex.getMessage()));
    }

    // 其他异常
    @ExceptionHandler(Exception.class)
    public ResponseEntity <DataInfo<Void>> handleAny(HttpServletRequest req, Exception ex) {
        log.error("Unhandled error on {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(DataInfo.fail("服务器内部错误"));
    }

}
