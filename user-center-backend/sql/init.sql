-- =====================================================
-- 用户中心数据库初始化脚本
-- =====================================================

-- 1. 创建数据库
DROP DATABASE IF EXISTS user_center;
CREATE DATABASE user_center
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE user_center;

-- =====================================================
-- 2. 用户表
-- =====================================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `user_account` VARCHAR(50) NOT NULL COMMENT '用户账号(登录用)',
    `user_password` VARCHAR(100) NOT NULL COMMENT '用户密码(MD5加密)',
    `gender` TINYINT DEFAULT 0 COMMENT '性别(0-未知,1-男,2-女)',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话号码',
    `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱地址',
    `avatar_url` LONGTEXT DEFAULT NULL COMMENT '头像URL(Base64编码)',
    `user_profile` VARCHAR(500) DEFAULT NULL COMMENT '用户简介',
    `user_role` VARCHAR(20) DEFAULT 'user' COMMENT '用户角色(user-普通用户,admin-管理员)',
    `is_delete` TINYINT DEFAULT 0 COMMENT '是否删除(0-否,1-是)',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_account` (`user_account`),
    KEY `idx_username` (`username`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- =====================================================
-- 3. 初始化测试数据（密码：123456）
-- 密码 md5(123456user_center) = a0a55d4d9c8a2d8abd2091c27ca215e5
-- =====================================================
INSERT INTO `user`
    (username, user_account, user_password, gender, user_role)
VALUES
    ('管理员', 'admin', 'a0a55d4d9c8a2d8abd2091c27ca215e5', 1, 'admin'),
    ('测试用户', 'test', 'a0a55d4d9c8a2d8abd2091c27ca215e5', 1, 'user'),
    ('示例用户', 'example', 'a0a55d4d9c8a2d8abd2091c27ca215e5', 2, 'user');

-- =====================================================
-- 4. 验证数据
-- =====================================================
SELECT * FROM `user`;
SELECT COUNT(*) AS user_count FROM `user`;

