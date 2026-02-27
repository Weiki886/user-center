package com.weiki.usercenter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis限流服务
 *
 * 功能：
 * - 基于Redis的接口限流
 * - 防止暴力破解和DDoS攻击
 */
@Service
@Slf4j
public class RedisRateLimitService {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 限流Key前缀
     */
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    /**
     * 默认限流时间窗口（秒）
     */
    private static final int DEFAULT_WINDOW_SECONDS = 60;

    /**
     * 默认最大请求次数
     */
    private static final int DEFAULT_MAX_REQUESTS = 10;

    public RedisRateLimitService(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 检查是否超过限流阈值
     *
     * @param key 限流key（通常是用户ID或IP地址）
     * @return true-允许访问，false-被限流
     */
    public boolean isAllowed(String key) {
        return isAllowed(key, DEFAULT_MAX_REQUESTS, DEFAULT_WINDOW_SECONDS);
    }

    /**
     * 检查是否超过限流阈值（自定义参数）
     *
     * @param key 限流key
     * @param maxRequests 最大请求次数
     * @param windowSeconds 时间窗口（秒）
     * @return true-允许访问，false-被限流
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;

        try {
            // 尝试获取当前计数
            String countStr = stringRedisTemplate.opsForValue().get(rateLimitKey);

            if (countStr != null) {
                long count = Long.parseLong(countStr);
                if (count >= maxRequests) {
                    log.warn("请求被限流，Key：{}，限制：{}次/{}秒", key, maxRequests, windowSeconds);
                    return false;
                }

                // 自增
                Long newCount = stringRedisTemplate.opsForValue().increment(rateLimitKey);
                log.debug("请求通过限流检测，Key：{}，当前次数：{}/{}", key, newCount, maxRequests);
                return true;
            } else {
                // 第一次请求，设置计数和过期时间
                stringRedisTemplate.opsForValue().set(rateLimitKey, "1", windowSeconds, TimeUnit.SECONDS);
                log.debug("请求通过限流检测，Key：{}，当前次数：1/{}", key, maxRequests);
                return true;
            }
        } catch (Exception e) {
            log.error("Redis限流异常，Key：{}，错误：{}", key, e.getMessage());
            return true; // 异常时默认允许访问
        }
    }

    /**
     * 获取当前请求次数
     *
     * @param key 限流key
     * @return 当前剩余请求次数（-1表示不存在或已过期）
     */
    public long getCurrentCount(String key) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        String countStr = stringRedisTemplate.opsForValue().get(rateLimitKey);
        return countStr != null ? Long.parseLong(countStr) : -1;
    }

    /**
     * 获取限流Key的剩余时间（秒）
     *
     * @param key 限流key
     * @return 剩余秒数（-1表示不过期，-2表示不存在）
     */
    public long getTTL(String key) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        Long ttl = stringRedisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }

    /**
     * 重置限流计数器
     *
     * @param key 限流key
     */
    public void resetLimit(String key) {
        String rateLimitKey = RATE_LIMIT_PREFIX + key;
        stringRedisTemplate.delete(rateLimitKey);
        log.info("限流计数器已重置，Key：{}", key);
    }

    /**
     * 检查登录是否被锁定（5次失败后锁定15分钟）
     * 注意：此方法只检查不增加计数，计数由 recordLoginFailure 方法增加
     *
     * @param account 账号
     * @return true-允许尝试，false-被锁定
     */
    public boolean isLoginAllowed(String account) {
        String rateLimitKey = RATE_LIMIT_PREFIX + "login:fail:" + account;

        try {
            // 只检查当前计数，不增加
            String countStr = stringRedisTemplate.opsForValue().get(rateLimitKey);

            if (countStr != null) {
                long count = Long.parseLong(countStr);
                if (count >= 5) {
                    log.warn("登录尝试被锁定，账号：{}，失败次数：{}", account, count);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            log.error("检查登录锁定状态异常，账号：{}，错误：{}", account, e.getMessage());
            return true; // 异常时默认允许访问
        }
    }

    /**
     * 记录登录失败次数
     * 注意：此方法只增加计数，不检查是否超过限制
     * 限制检查由 isLoginAllowed 方法负责
     *
     * @param account 账号
     */
    public void recordLoginFailure(String account) {
        String rateLimitKey = RATE_LIMIT_PREFIX + "login:fail:" + account;
        try {
            Long count = stringRedisTemplate.opsForValue().increment(rateLimitKey);
            if (count != null && count == 1) {
                // 第一次失败，设置过期时间（15分钟）
                stringRedisTemplate.expire(rateLimitKey, 900, TimeUnit.SECONDS);
            }
            log.warn("登录失败，账号：{}，失败次数：{}", account, count);
        } catch (Exception e) {
            log.error("记录登录失败次数失败，账号：{}", account, e);
        }
    }

    /**
     * 清除登录失败记录
     *
     * @param account 账号
     */
    public void clearLoginFailures(String account) {
        resetLimit("login:fail:" + account);
    }

    /**
     * 获取登录失败次数
     *
     * @param account 账号
     * @return 失败次数
     */
    public long getLoginFailCount(String account) {
        return getCurrentCount("login:fail:" + account);
    }
}


