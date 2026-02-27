package com.weiki.usercenter.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名（用于展示）
     */
    private String username;
    
    /**
     * 账号（用于登录）
     */
    private String userAccount;
    
    /**
     * 密码（加密后的密码）
     */
    private String userPassword;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 电话号码
     */
    private String phone;
    
    /**
     * 邮箱地址
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
    
    /**
     * 用户角色：user-普通用户，admin-管理员
     */
    private String userRole;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer isDelete;
}


