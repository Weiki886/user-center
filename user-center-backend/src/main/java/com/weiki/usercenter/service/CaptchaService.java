package com.weiki.usercenter.service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * 验证码服务
 */
public class CaptchaService {

    /**
     * 验证码长度
     */
    private static final int CAPTCHA_LENGTH = 4;

    /**
     * 验证码过期时间（秒）
     */
    private static final int CAPTCHA_EXPIRE_SECONDS = 300;

    /**
     * 验证码图片宽度
     */
    private static final int IMAGE_WIDTH = 120;

    /**
     * 验证码图片高度
     */
    private static final int IMAGE_HEIGHT = 40;

    /**
     * 验证码字符集
     */
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";

    /**
     * 字体
     */
    private static final Font FONT = new Font("Arial", Font.BOLD, 24);

    /**
     * 生成验证码
     *
     * @return 验证码字符串
     */
    public static String generateCaptchaCode() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder();
        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            captcha.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return captcha.toString();
    }

    /**
     * 生成验证码图片
     *
     * @param captchaCode 验证码
     * @return 验证码图片
     */
    public static BufferedImage generateCaptchaImage(String captchaCode) {
        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);

        // 设置字体
        g.setFont(FONT);

        // 绘制验证码字符
        Random random = new Random();
        for (int i = 0; i < captchaCode.length(); i++) {
            // 随机颜色
            g.setColor(new Color(
                random.nextInt(150),
                random.nextInt(150),
                random.nextInt(150)
            ));

            // 随机旋转角度
            double angle = random.nextDouble() * 0.4 - 0.2;
            g.rotate(angle, (IMAGE_WIDTH / CAPTCHA_LENGTH) * i + 15, IMAGE_HEIGHT / 2);

            // 绘制字符
            g.drawString(String.valueOf(captchaCode.charAt(i)), (IMAGE_WIDTH / CAPTCHA_LENGTH) * i + 10, IMAGE_HEIGHT / 2 + 8);

            // 旋转回来
            g.rotate(-angle, (IMAGE_WIDTH / CAPTCHA_LENGTH) * i + 15, IMAGE_HEIGHT / 2);
        }

        // 添加干扰线
        for (int i = 0; i < 5; i++) {
            g.setColor(new Color(
                random.nextInt(200),
                random.nextInt(200),
                random.nextInt(200)
            ));
            g.drawLine(
                random.nextInt(IMAGE_WIDTH),
                random.nextInt(IMAGE_HEIGHT),
                random.nextInt(IMAGE_WIDTH),
                random.nextInt(IMAGE_HEIGHT)
            );
        }

        // 添加噪点
        for (int i = 0; i < 30; i++) {
            g.setColor(new Color(
                random.nextInt(255),
                random.nextInt(255),
                random.nextInt(255)
            ));
            g.drawLine(
                random.nextInt(IMAGE_WIDTH),
                random.nextInt(IMAGE_HEIGHT),
                random.nextInt(IMAGE_WIDTH),
                random.nextInt(IMAGE_HEIGHT)
            );
        }

        g.dispose();
        return image;
    }

    /**
     * 获取验证码过期时间
     *
     * @return 过期时间（秒）
     */
    public static int getExpireSeconds() {
        return CAPTCHA_EXPIRE_SECONDS;
    }
}


