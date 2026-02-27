package com.weiki.usercenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 用户注册DTO
 */
@Data
public class UserRegisterDTO {
    
    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    private String username;
    
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
     * 确认密码
     * 注意：不做后端校验，前端已有密码一致性检查
     */
    private String checkPassword;

    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像URL
     */
    private String avatarUrl;

    /**
     * 验证码ID
     * 注意：不做后端必填校验，前端可能验证码还没加载完就提交
     */
    private String captchaId;

    /**
     * 验证码答案
     */
    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}


