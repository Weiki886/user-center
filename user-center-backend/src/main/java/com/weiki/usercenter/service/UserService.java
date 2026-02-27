package com.weiki.usercenter.service;

import com.weiki.usercenter.dto.*;
import com.weiki.usercenter.entity.PageVO;
import com.weiki.usercenter.entity.User;

import java.util.List;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 用户注册
     */
    Long register(UserRegisterDTO registerDTO);
    
    /**
     * 用户登录
     */
    LoginVO login(UserLoginDTO loginDTO);
    
    /**
     * 用户登出
     */
    void logout(Long userId);
    
    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);
    
    /**
     * 获取所有用户
     */
    List<UserVO> getAllUsers();
    
    /**
     * 分页获取用户列表
     */
    PageVO<UserVO> getUsersByPage(UserQueryDTO queryDTO);
    
    /**
     * 根据用户名搜索用户
     */
    List<UserVO> searchUsers(String username);
    
    /**
     * 根据账号获取用户
     */
    User getUserByAccount(String account);
    
    /**
     * 更新用户信息
     */
    boolean updateUser(Long id, UserUpdateDTO updateDTO);
    
    /**
     * 更新用户密码
     */
    boolean updatePassword(Long id, String oldPassword, String newPassword);
    
    /**
     * 管理员重置用户密码
     */
    boolean resetPassword(Long id, String newPassword);
    
    /**
     * 删除用户（逻辑删除）
     */
    boolean deleteUser(Long id);

    /**
     * 获取当前登录用户信息（用于验证 token 是否有效）
     */
    UserVO getCurrentUser();
}