package com.weiki.usercenter.controller;

import com.weiki.usercenter.annotation.RequireRole;
import com.weiki.usercenter.dto.*;
import com.weiki.usercenter.entity.PageVO;
import com.weiki.usercenter.entity.Result;
import com.weiki.usercenter.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 用户相关接口控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    private final UserService userService;
    
    public UserController(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * 用户注册接口 - 公开接口
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public Result<Long> register(@Valid @RequestBody UserRegisterDTO registerDTO) {
        Long userId = userService.register(registerDTO);
        return Result.success("注册成功", userId);
    }
    
    /**
     * 用户登录接口 - 公开接口
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public Result<LoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success("登录成功", loginVO);
    }
    
    /**
     * 用户登出接口 - 需要登录
     */
    @PostMapping("/logout")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("用户登出")
    public Result<Void> logout(@RequestParam Long userId) {
        userService.logout(userId);
        return Result.success("登出成功");
    }

    /**
     * 获取当前登录用户信息 - 需要登录（用于验证 token 是否有效）
     */
    @GetMapping("/current")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("获取当前登录用户信息")
    public Result<UserVO> getCurrentUser() {
        UserVO user = userService.getCurrentUser();
        return Result.success(user);
    }
    
    /**
     * 获取用户详情 - 需要登录
     */
    @GetMapping("/{id}")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("获取用户详情")
    public Result<UserVO> getUserById(@ApiParam("用户ID") @PathVariable Long id) {
        UserVO user = userService.getUserById(id);
        return Result.success(user);
    }
    
    /**
     * 获取用户列表 - 需要管理员权限
     */
    @GetMapping
    @RequireRole(role = "admin", requireLogin = true)
    @ApiOperation("获取用户列表")
    public Result<List<UserVO>> getAllUsers() {
        List<UserVO> users = userService.getAllUsers();
        return Result.success(users);
    }
    
    /**
     * 分页获取用户列表 - 需要管理员权限
     */
    @GetMapping("/page")
    @RequireRole(role = "admin", requireLogin = true)
    @ApiOperation("分页获取用户列表")
    public Result<PageVO<UserVO>> getUsersByPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username) {
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setUsername(username);
        PageVO<UserVO> pageVO = userService.getUsersByPage(queryDTO);
        return Result.success(pageVO);
    }
    
    /**
     * 搜索用户 - 需要管理员权限
     */
    @GetMapping("/search")
    @RequireRole(role = "admin", requireLogin = true)
    @ApiOperation("搜索用户")
    public Result<List<UserVO>> searchUsers(@ApiParam("用户名") @RequestParam String username) {
        List<UserVO> users = userService.searchUsers(username);
        return Result.success(users);
    }
    
    /**
     * 更新用户信息 - 普通用户只能修改自己的信息，管理员可以修改任意用户信息
     */
    @PutMapping("/{id}")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("更新用户信息")
    public Result<Boolean> updateUser(
            @ApiParam("用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        boolean success = userService.updateUser(id, updateDTO);
        return Result.success("更新成功", success);
    }
    
    /**
     * 更新用户密码 - 需要登录
     */
    @PutMapping("/{id}/password")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("修改用户密码")
    public Result<Boolean> updatePassword(
            @ApiParam("用户ID") @PathVariable Long id,
            @ApiParam("旧密码") @RequestParam String oldPassword,
            @ApiParam("新密码") @RequestParam String newPassword) {
        boolean success = userService.updatePassword(id, oldPassword, newPassword);
        return Result.success("密码更新成功", success);
    }
    
    /**
     * 管理员重置用户密码 - 需要管理员权限
     */
    @PutMapping("/{id}/reset-password")
    @RequireRole(role = "admin", requireLogin = true)
    @ApiOperation("管理员重置用户密码")
    public Result<Boolean> resetPassword(
            @ApiParam("用户ID") @PathVariable Long id,
            @Valid @RequestBody PasswordResetDTO resetDTO) {
        boolean success = userService.resetPassword(id, resetDTO.getNewPassword());
        return Result.success("密码重置成功", success);
    }
    
    /**
     * 删除用户 - 普通用户只能删除自己的账号，管理员可以删除任意用户账号
     */
    @DeleteMapping("/{id}")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("删除用户")
    public Result<Boolean> deleteUser(@ApiParam("用户ID") @PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        return Result.success("删除成功", success);
    }
}