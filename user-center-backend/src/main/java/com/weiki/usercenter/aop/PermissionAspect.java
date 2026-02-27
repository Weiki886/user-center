package com.weiki.usercenter.aop;

import com.weiki.usercenter.annotation.RequireRole;
import com.weiki.usercenter.entity.User;
import com.weiki.usercenter.exception.BusinessException;
import com.weiki.usercenter.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 权限校验 AOP 切面
 * 通过 @RequireRole 注解控制方法的访问权限
 */
@Slf4j
@Aspect
@Component
public class PermissionAspect {

    /**
     * 角色等级定义
     */
    private static final int ROLE_LEVEL_USER = 1;
    private static final int ROLE_LEVEL_ADMIN = 5;

    /**
     * 定义切点：所有带有 @RequireRole 注解的方法
     */
    @Pointcut("@annotation(com.weiki.usercenter.annotation.RequireRole)")
    public void permissionPointcut() {}

    /**
     * 前置通知：检查权限
     */
    @Before("permissionPointcut() && @annotation(requireRole)")
    public void checkPermission(JoinPoint joinPoint, RequireRole requireRole) {
        // 获取当前请求
        HttpServletRequest request = getRequest();
        if (request == null) {
            throw new BusinessException("无法获取请求信息");
        }

        // 检查是否需要登录
        if (requireRole.requireLogin()) {
            User currentUser = SecurityUtils.getCurrentUser();
            if (currentUser == null) {
                log.warn("用户未登录，尝试访问受限接口: {}", request.getRequestURI());
                // 抛出异常由全局异常处理器处理返回 401
                throw new BusinessException(401, "请先登录");
            }

            // 检查角色权限
            String requiredRole = requireRole.role();
            int requiredLevel = getRoleLevel(requiredRole);
            int userLevel = getRoleLevel(currentUser.getUserRole());

            if (userLevel < requiredLevel) {
                log.warn("用户 {} 权限不足，需要 {} 权限", currentUser.getUserAccount(), requiredRole);
                throw new BusinessException(403, "权限不足，需要 " + requiredRole + " 角色");
            }

            log.debug("用户 {} 通过权限校验，角色: {}", currentUser.getUserAccount(), currentUser.getUserRole());
        }
    }

    /**
     * 获取当前 HTTP 请求
     */
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            log.error("获取请求失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 获取角色的等级
     */
    private int getRoleLevel(String role) {
        if (role == null) {
            return ROLE_LEVEL_USER;
        }
        switch (role.toLowerCase()) {
            case "admin":
                return ROLE_LEVEL_ADMIN;
            case "user":
            default:
                return ROLE_LEVEL_USER;
        }
    }
}


