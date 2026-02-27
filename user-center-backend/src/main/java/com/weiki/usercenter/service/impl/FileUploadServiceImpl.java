package com.weiki.usercenter.service.impl;

import cn.hutool.core.util.IdUtil;
import com.weiki.usercenter.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传服务实现类
 */
@Service
@Slf4j
public class FileUploadServiceImpl implements FileUploadService {

    /**
     * 头像存储目录
     */
    @Value("${file.upload.avatar-path:./uploads/avatar}")
    private String avatarUploadPath;

    /**
     * 头像访问URL前缀
     */
    @Value("${file.upload.avatar-url:/api/avatar}")
    private String avatarUrlPrefix;

    /**
     * 允许的图片类型
     */
    private static final List<String> ALLOWED_IMAGE_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 允许的图片扩展名
     */
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp"
    );

    @Override
    public String uploadAvatar(MultipartFile file, Long userId) {
        // 1. 校验文件是否为空
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件不能为空");
        }

        // 2. 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        // 3. 校验文件扩展名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("文件名无效");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("只支持 JPG、PNG、GIF、WebP 格式的图片");
        }

        // 4. 校验文件大小（最大 10MB）
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过 10MB");
        }

        try {
            // 5. 创建上传目录 - 使用绝对路径
            String basePath = System.getProperty("user.dir");
            File uploadDir = new File(basePath + File.separator + avatarUploadPath.replace("./", "uploads" + File.separator));
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            log.info("头像上传目录: {}", uploadDir.getAbsolutePath());

            // 6. 生成新的文件名（使用Hutool）
            String newFileName = userId + "_" + IdUtil.simpleUUID().substring(0, 8) + extension;
            File targetFile = new File(uploadDir, newFileName);

            // 7. 保存文件
            file.transferTo(targetFile);
            log.info("头像上传成功，用户ID：{}，文件路径：{}", userId, targetFile.getAbsolutePath());

            // 8. 返回访问URL
            return avatarUrlPrefix + "/" + newFileName;

        } catch (IOException e) {
            log.error("头像上传失败，用户ID：{}", userId, e);
            throw new RuntimeException("头像上传失败，请稍后重试");
        }
    }
}