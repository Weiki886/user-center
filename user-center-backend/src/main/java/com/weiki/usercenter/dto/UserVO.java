package com.weiki.usercenter.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户VO（视图对象）
 */
@Data
public class UserVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 账号
     */
    private String userAccount;
    
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
    
    /**
     * 用户角色
     */
    private String userRole;
    
    /**
     * 创建时间
     */
    private String createTime;
}


