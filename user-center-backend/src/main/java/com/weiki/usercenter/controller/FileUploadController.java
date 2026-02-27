package com.weiki.usercenter.controller;

import com.weiki.usercenter.annotation.RequireRole;
import com.weiki.usercenter.dto.UserUpdateDTO;
import com.weiki.usercenter.entity.Result;
import com.weiki.usercenter.service.FileUploadService;
import com.weiki.usercenter.service.UserService;
import com.weiki.usercenter.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器
 */
@Api(tags = "文件上传")
@RestController
@RequestMapping("/file")
@Slf4j
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final UserService userService;

    public FileUploadController(FileUploadService fileUploadService, UserService userService) {
        this.fileUploadService = fileUploadService;
        this.userService = userService;
    }

    /**
     * 上传用户头像 - 需要登录
     * 普通用户只能修改自己的头像，管理员可以修改任意用户的头像
     */
    @PostMapping("/avatar/{userId}")
    @RequireRole(role = "user", requireLogin = true)
    @ApiOperation("上传用户头像")
    public Result<String> uploadAvatar(
            @ApiParam("用户ID") @PathVariable Long userId,
            @ApiParam("头像文件") @RequestParam("file") MultipartFile file) {

        // 权限校验：普通用户只能修改自己的头像，管理员可以修改任意用户的头像
        Long currentUserId = SecurityUtils.getCurrentUserId();
        String currentUserRole = SecurityUtils.getCurrentUserRole();

        if (!"admin".equals(currentUserRole) && !currentUserId.equals(userId)) {
            return Result.error("没有权限修改其他用户的头像");
        }

        // 上传头像
        String avatarUrl = fileUploadService.uploadAvatar(file, userId);

        // 更新用户头像到数据库
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setAvatarUrl(avatarUrl);
        userService.updateUser(userId, updateDTO);

        log.info("用户头像上传成功，用户ID：{}，头像URL：{}", userId, avatarUrl);

        return Result.success("头像上传成功", avatarUrl);
    }
}
