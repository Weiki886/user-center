package com.weiki.usercenter.service;

import com.weiki.usercenter.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * Redis Token管理服务
 *
 * 功能：
 * - Token存储到Redis，支持分布式环境
 * - Token自动过期
 * - 主动登出（删除Token）
 * - 支持查询Token对应的用户信息
 */
@Service
@Slf4j
public class RedisTokenService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Token Key前缀
     */
    private static final String TOKEN_KEY_PREFIX = "token:";

    /**
     * 用户Token索引前缀（根据用户ID查找Token）
     */
    private static final String USER_TOKEN_PREFIX = "user:token:";

    /**
     * Token过期时间（毫秒）
     */
    private static final long TOKEN_EXPIRE_MILLIS = 24 * 60 * 60 * 1000; // 24小时

    public RedisTokenService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存储Token
     *
     * @param token Token字符串
     * @param user 用户信息
     */
    public void saveToken(String token, User user) {
        // 存储Token与用户信息的映射
        String tokenKey = TOKEN_KEY_PREFIX + token;
        redisTemplate.opsForValue().set(tokenKey, user, TOKEN_EXPIRE_MILLIS, TimeUnit.MILLISECONDS);

        // 存储用户ID与Token的映射（一个用户可能有多个Token）
        String userTokenKey = USER_TOKEN_PREFIX + user.getId();
        redisTemplate.opsForSet().add(userTokenKey, token);

        log.debug("Token已存储，用户ID：{}，Token：{}", user.getId(), token);
    }

    /**
     * 验证Token并获取用户信息
     *
     * @param token Token字符串
     * @return 用户信息（如果Token无效返回null）
     */
    public User validateToken(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        Object userObj = redisTemplate.opsForValue().get(tokenKey);

        if (userObj == null) {
            log.debug("Token无效或已过期：{}", token);
            return null;
        }

        // 刷新Token过期时间
        redisTemplate.expire(tokenKey, TOKEN_EXPIRE_MILLIS, TimeUnit.MILLISECONDS);

        if (userObj instanceof User) {
            return (User) userObj;
        }

        return null;
    }

    /**
     * 删除Token（登出）
     *
     * @param token Token字符串
     * @param userId 用户ID
     */
    public void deleteToken(String token, Long userId) {
        // 删除Token
        String tokenKey = TOKEN_KEY_PREFIX + token;
        redisTemplate.delete(tokenKey);

        // 从用户Token集合中移除
        if (userId != null) {
            String userTokenKey = USER_TOKEN_PREFIX + userId;
            redisTemplate.opsForSet().remove(userTokenKey, token);
        }

        log.debug("Token已删除，UserID：{}，Token：{}", userId, token);
    }

    /**
     * 删除用户的所有Token（强制登出所有设备）
     *
     * @param userId 用户ID
     */
    public void deleteUserTokens(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;

        // 获取用户的所有Token
        java.util.Set<Object> tokensSet = redisTemplate.opsForSet().members(userTokenKey);

        if (tokensSet != null && !tokensSet.isEmpty()) {
            // 删除所有Token
            for (Object token : tokensSet) {
                String tokenKey = TOKEN_KEY_PREFIX + token.toString();
                redisTemplate.delete(tokenKey);
            }
        }

        // 删除用户Token集合
        redisTemplate.delete(userTokenKey);

        log.info("用户所有Token已删除，UserID：{}", userId);
    }

    /**
     * 检查Token是否存在
     *
     * @param token Token字符串
     * @return 是否存在
     */
    public boolean exists(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey));
    }

    /**
     * 获取Token剩余存活时间（秒）
     *
     * @param token Token字符串
     * @return 剩余秒数（-1表示不过期，-2表示不存在）
     */
    public long getTTL(String token) {
        String tokenKey = TOKEN_KEY_PREFIX + token;
        Long ttl = redisTemplate.getExpire(tokenKey, TimeUnit.SECONDS);
        return ttl != null ? ttl : -2;
    }

    /**
     * 获取用户当前在线的Token数量
     *
     * @param userId 用户ID
     * @return Token数量
     */
    public Long getUserTokenCount(Long userId) {
        String userTokenKey = USER_TOKEN_PREFIX + userId;
        Long size = redisTemplate.opsForSet().size(userTokenKey);
        return size != null ? size : 0L;
    }
}


