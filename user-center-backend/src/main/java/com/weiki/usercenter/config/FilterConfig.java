package com.weiki.usercenter.config;

import com.weiki.usercenter.filter.AuthenticationFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 过滤器配置
 */
@Configuration
public class FilterConfig {

    /**
     * 注册认证过滤器
     * 
     * 过滤所有请求，确保每个请求都能进行Token认证
     */
    @Bean
    public FilterRegistrationBean<AuthenticationFilter> authenticationFilterRegistration(
            AuthenticationFilter authenticationFilter) {
        FilterRegistrationBean<AuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(authenticationFilter);
        // 设置过滤器优先级，越小越优先
        registration.setOrder(1);
        // 过滤所有请求
        registration.addUrlPatterns("/*");
        // 忽略某些URL（可选）
        registration.addInitParameter("exclusions", "/api/user/login,/api/user/register,/api/captcha/*,/api/avatar/*");
        return registration;
    }
}

