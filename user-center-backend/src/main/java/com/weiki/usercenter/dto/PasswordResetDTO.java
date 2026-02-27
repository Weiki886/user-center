package com.weiki.usercenter.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 密码重置DTO（管理员使用）
 */
@Data
public class PasswordResetDTO {

    /**
     * 新密码
     */
    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}

