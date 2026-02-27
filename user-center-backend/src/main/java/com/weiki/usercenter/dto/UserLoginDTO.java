package com.weiki.usercenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户登录DTO
 */
@Data
public class UserLoginDTO {
    
    /**
     * 账号
     */
    @NotBlank(message = "账号不能为空")
    private String userAccount;
    
    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
    private String userPassword;

    /**
     * 验证码ID（登录失败3次后需要）
     */
    private String captchaId;

    /**
     * 验证码答案（登录失败3次后需要）
     */
    private String captchaCode;
}


