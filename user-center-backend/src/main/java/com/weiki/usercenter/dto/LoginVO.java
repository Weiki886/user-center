package com.weiki.usercenter.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应VO
 */
@Data
public class LoginVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 登录凭证
     */
    private String token;
    
    /**
     * 用户信息
     */
    private UserVO user;
}


