package com.weiki.usercenter.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 角色权限注解
 * 用于方法级别控制访问权限
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    /**
     * 需要的角色
     * user - 普通用户
     * admin - 管理员
     */
    String role() default "user";
    
    /**
     * 是否需要登录
     */
    boolean requireLogin() default true;
}