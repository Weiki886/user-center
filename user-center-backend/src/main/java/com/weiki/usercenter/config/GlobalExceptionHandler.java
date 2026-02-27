package com.weiki.usercenter.config;

import com.weiki.usercenter.entity.Result;
import com.weiki.usercenter.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 从BindingResult中构建错误消息
     *
     * @param bindingResult 绑定结果
     * @param errorType 错误类型描述
     * @return 错误消息字符串
     */
    private String buildErrorMessage(org.springframework.validation.BindingResult bindingResult, String errorType) {
        List<FieldError> fieldErrors = bindingResult.getFieldErrors();
        StringBuilder errorMsg = new StringBuilder();
        for (FieldError error : fieldErrors) {
            errorMsg.append(error.getDefaultMessage());
            errorMsg.append("; ");
        }
        // 去除末尾的分号和空格
        return errorMsg.toString().replaceAll(";\\s*$", "");
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleValidException(MethodArgumentNotValidException e) {
        String msg = buildErrorMessage(e.getBindingResult(), "参数校验");
        log.error("参数校验失败：{}", msg);
        return Result.error(400, msg);
    }

    /**
     * 处理绑定异常
     */
    @ExceptionHandler(BindException.class)
    public Result<?> handleBindException(BindException e) {
        String msg = buildErrorMessage(e.getBindingResult(), "参数绑定");
        log.error("参数绑定失败：{}", msg);
        return Result.error(400, msg);
    }
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        log.warn("业务异常：{}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常：", e);
        return Result.error(-1, e.getMessage());
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(-1, "系统繁忙，请稍后重试");
    }
}


