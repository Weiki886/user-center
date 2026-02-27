package com.weiki.usercenter.dto;

import lombok.Data;

/**
 * 用户更新DTO
 */
@Data
public class UserUpdateDTO {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 性别
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
     * 用户简介
     */
    private String userProfile;
}


