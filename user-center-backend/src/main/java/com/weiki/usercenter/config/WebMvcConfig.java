package com.weiki.usercenter.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

/**
 * Web MVC 配置类
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 头像存储路径
     */
    @Value("${file.upload.avatar-path:./uploads/avatar}")
    private String avatarPath;

    /**
     * 头像访问URL前缀
     */
    @Value("${file.upload.avatar-url:/api/avatar}")
    private String avatarUrl;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 配置头像静态资源访问
        // 将 /api/avatar/** 映射到本地文件目录
        String basePath = System.getProperty("user.dir");
        String avatarDir = avatarPath.replace("./", "uploads" + File.separator);
        String avatarLocation = "file:" + basePath + File.separator + avatarDir + File.separator;
        
        // 确保目录存在
        File dir = new File(basePath + File.separator + avatarDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        log.info("头像静态资源路径: {}", avatarLocation);
        registry.addResourceHandler(avatarUrl + "/**")
                .addResourceLocations(avatarLocation);
    }
}

