package com.weiki.usercenter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 用户中心项目启动类
 * 
 * @SpringBootApplication 是一个组合注解，包含：
 * - @Configuration：标记为配置类
 * - @EnableAutoConfiguration：启用自动配置
 * - @ComponentScan：扫描组件
 */
@SpringBootApplication
// 指定Mapper接口扫描路径
@MapperScan("com.weiki.usercenter.mapper")
public class UserCenterApplication {
    
    /**
     * main方法：程序入口
     */
    public static void main(String[] args) {
        // 启动Spring Boot应用
        SpringApplication.run(UserCenterApplication.class, args);
        
        // 启动成功后打印信息
        System.out.println("=====================================");
        System.out.println("    用户中心后端服务启动成功！");
        System.out.println("    API文档：http://localhost:8080/api/doc.html");
        System.out.println("=====================================");
    }
}


