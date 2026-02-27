package com.weiki.usercenter.filter;

import com.weiki.usercenter.entity.User;
import com.weiki.usercenter.service.RedisTokenService;
import com.weiki.usercenter.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证过滤器
 * 
 * 功能：
 * - 从请求头获取 Token
 * - 验证 Token 并从 Redis 获取用户信息
 * - 将用户信息设置到 SecurityContext（供后续使用）
 */
@Component
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    private final RedisTokenService redisTokenService;

    public AuthenticationFilter(RedisTokenService redisTokenService) {
        this.redisTokenService = redisTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 使用 SecurityUtils 获取 Token（统一Token获取逻辑）
        String token = SecurityUtils.getToken();

        // 2. 如果有 Token，验证并获取用户信息
        if (token != null && !token.isEmpty()) {
            try {
                User user = redisTokenService.validateToken(token);
                if (user != null) {
                    // 3. 将用户信息设置到 SecurityContext（供后续使用）
                    SecurityUtils.setCurrentUser(user);
                    log.debug("Token认证成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());
                }
            } catch (Exception e) {
                log.warn("Token认证失败：{}", e.getMessage());
            }
        }

        // 4. 继续执行后续过滤器
        filterChain.doFilter(request, response);
    }
}

