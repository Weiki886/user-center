package com.weiki.usercenter.dto;

import lombok.Data;

/**
 * 验证码VO
 */
@Data
public class CaptchaVO {

    /**
     * 验证码ID
     */
    private String captchaId;

    /**
     * 验证码图片（Base64格式）
     */
    private String captchaImage;
}