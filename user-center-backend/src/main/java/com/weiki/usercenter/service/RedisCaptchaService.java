package com.weiki.usercenter.service;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

/**
 * Redis验证码服务
 *
 * 功能：
 * - 生成验证码图片并存储到Redis
 * - 验证验证码
 * - 验证码自动过期
 */
@Service
@Slf4j
public class RedisCaptchaService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 验证码Key前缀
     */
    private static final String CAPTCHA_KEY_PREFIX = "captcha:";

    /**
     * 验证码过期时间（秒）
     */
    private static final long CAPTCHA_EXPIRE_SECONDS = 300;

    public RedisCaptchaService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 验证码结果封装
     */
    public static class CaptchaResult {
        private String captchaId;
        private String captchaImage;

        public CaptchaResult(String captchaId, String captchaImage) {
            this.captchaId = captchaId;
            this.captchaImage = captchaImage;
        }

        public String getCaptchaId() {
            return captchaId;
        }

        public String getCaptchaImage() {
            return captchaImage;
        }
    }

    /**
     * 生成并存储验证码
     *
     * @return 验证码结果，包含ID和Base64图片
     */
    public CaptchaResult generateCaptcha() {
        // 生成验证码ID
        String captchaId = IdUtil.simpleUUID();

        // 生成验证码答案和图片
        String captchaCode = CaptchaService.generateCaptchaCode();
        BufferedImage captchaImage = CaptchaService.generateCaptchaImage(captchaCode);

        // 转换为Base64
        String captchaImageBase64 = imageToBase64(captchaImage);

        // 存储到Redis
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.opsForValue().set(key, captchaCode, CAPTCHA_EXPIRE_SECONDS, TimeUnit.SECONDS);

        log.debug("验证码已生成，ID：{}", captchaId);

        // 返回验证码ID和Base64图片
        return new CaptchaResult(captchaId, captchaImageBase64);
    }

    /**
     * 验证验证码
     *
     * @param captchaId 验证码ID
     * @param userInput 用户输入的验证码
     * @return 是否正确
     */
    public boolean verifyCaptcha(String captchaId, String userInput) {
        if (captchaId == null || userInput == null) {
            log.warn("验证码验证失败：参数为空");
            return false;
        }

        String key = CAPTCHA_KEY_PREFIX + captchaId;
        Object storedCode = redisTemplate.opsForValue().get(key);

        if (storedCode == null) {
            log.warn("验证码已过期，ID：{}", captchaId);
            return false;
        }

        // 验证成功后删除验证码（防止重复使用）
        boolean result = userInput.equalsIgnoreCase(storedCode.toString());
        if (result) {
            redisTemplate.delete(key);
            log.debug("验证码验证成功，已删除，ID：{}", captchaId);
        } else {
            log.warn("验证码验证失败，ID：{}", captchaId);
        }

        return result;
    }

    /**
     * 移除验证码
     *
     * @param captchaId 验证码ID
     */
    public void removeCaptcha(String captchaId) {
        String key = CAPTCHA_KEY_PREFIX + captchaId;
        redisTemplate.delete(key);
    }

    /**
     * 图片转Base64
     *
     * @param image 图片
     * @return Base64字符串
     */
    private String imageToBase64(BufferedImage image) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            javax.imageio.ImageIO.write(image, "PNG", baos);
            byte[] bytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(bytes);
        } catch (Exception e) {
            log.error("图片转Base64失败", e);
            throw new RuntimeException("验证码生成失败");
        }
    }
}
