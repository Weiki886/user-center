package com.weiki.usercenter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Knife4j/Swagger 配置类
 * 用于排除 /error 接口，并配置 API 文档信息
 */
@Configuration
@EnableOpenApi
public class SwaggerConfig {

    /**
     * 主 Docket Bean
     * 配置 API 文档基本信息，并排除 /error 接口
     */
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                // 排除 /error 接口
                .pathMapping("/")
                .select()
                // 扫描 com.weiki.usercenter 包下的接口
                .apis(RequestHandlerSelectors.basePackage("com.weiki.usercenter"))
                // 排除 error 路径
                .paths(PathSelectors.regex("^(?!/error).*$"))
                .build();
    }

    /**
     * API 文档基本信息
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("用户中心系统 API 文档")
                .description("用户中心系统后端接口文档，提供用户管理、文件上传等功能")
                .version("1.0.0")
                .build();
    }
}