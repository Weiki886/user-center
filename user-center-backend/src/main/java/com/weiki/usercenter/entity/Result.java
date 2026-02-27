package com.weiki.usercenter.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 */
@Data
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应码
     * 0-成功，其他-失败
     */
    private int code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 响应时间戳
     */
    private long timestamp;
    
    public Result() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    
    // 成功响应方法
    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }
    
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }
    
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(0, message, data);
    }
    
    public static <T> Result<T> success(String message) {
        return new Result<>(0, message, null);
    }
    
    // 失败响应方法
    public static <T> Result<T> error(String message) {
        return new Result<>(-1, message, null);
    }
    
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return this.code == 0;
    }
}


