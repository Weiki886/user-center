package com.weiki.usercenter.utils;

import com.weiki.usercenter.entity.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public class SecurityUtils {

    private static final String CURRENT_USER_KEY = "currentUser";

    public static HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static HttpSession getSession() {
        HttpServletRequest request = getRequest();
        return request != null ? request.getSession() : null;
    }

    public static String getToken() {
        HttpServletRequest request = getRequest();
        if (request == null) return null;
        String token = request.getHeader("Authorization");
        if (token != null && !token.isEmpty()) {
            return token;
        }
        return request.getParameter("token");
    }

    public static String getClientIP() {
        HttpServletRequest request = getRequest();
        if (request == null) return "unknown";

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    public static void setSessionAttribute(String key, Object value) {
        HttpSession session = getSession();
        if (session != null) {
            session.setAttribute(key, value);
        }
    }

    public static Object getSessionAttribute(String key) {
        HttpSession session = getSession();
        return session != null ? session.getAttribute(key) : null;
    }

    public static User getCurrentUser() {
        return (User) getSessionAttribute(CURRENT_USER_KEY);
    }

    public static void setCurrentUser(User user) {
        setSessionAttribute(CURRENT_USER_KEY, user);
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 获取当前用户角色
     */
    public static String getCurrentUserRole() {
        User user = getCurrentUser();
        return user != null ? user.getUserRole() : null;
    }
}


