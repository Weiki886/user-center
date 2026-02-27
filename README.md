# 用户中心系统 (User Center)

一个前后端分离的用户中心管理系统，提供完整的用户注册、登录、权限管理等功能。

## 项目简介

本项目是一个通用的用户中心后端服务，采用 RESTful API 设计，支持用户 CRUD 操作、验证码登录、文件上传等功能。前端使用 Vue 3 + Ant Design Vue 构建，提供现代化的管理界面。

## 技术栈

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.18 | 核心框架 |
| Java | 17 | 开发语言 |
| MyBatis | 2.3.2 | ORM 持久层框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | - | 缓存数据库 |
| Knife4j | 2.0.9 | API 文档生成 |
| Hutool | 5.8.23 | 工具类库 |
| Spring AOP | - | 面向切面编程 |

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.0 | 渐进式前端框架 |
| Vue Router | 4.2.5 | 路由管理 |
| Pinia | 2.1.7 | 状态管理 |
| Ant Design Vue | 4.0.0 | UI 组件库 |
| Axios | 1.6.2 | HTTP 客户端 |
| Vite | 5.0.8 | 构建工具 |
| Sass | 1.69.5 | CSS 预处理器 |

## 功能特性

### 用户管理
- 用户注册与登录
- 用户信息查询与修改
- 用户删除与禁用
- 分页查询用户列表

### 认证授权
- JWT Token 认证
- 验证码登录
- 基于角色的权限控制 (RBAC)
- 接口限流保护

### 文件管理
- 用户头像上传
- 文件存储服务

### API 文档
- Knife4j 在线 API 文档
- 接口在线测试

## 项目结构

```
user-center/
├── user-center-backend/          # 后端项目
│   ├── src/main/java/com/weiki/usercenter/
│   │   ├── annotation/           # 自定义注解
│   │   ├── aop/                  # 切面编程
│   │   ├── config/               # 配置类
│   │   ├── controller/           # 控制器
│   │   ├── dto/                  # 数据传输对象
│   │   ├── entity/               # 实体类
│   │   ├── exception/            # 异常处理
│   │   ├── filter/               # 过滤器
│   │   ├── mapper/               # 数据访问层
│   │   ├── service/              # 业务逻辑层
│   │   └── utils/                # 工具类
│   ├── src/main/resources/
│   │   ├── mapper/               # MyBatis 映射文件
│   │   └── application.yml       # 配置文件
│   └── sql/                      # 数据库脚本
│
└── user-center-frontend/         # 前端项目
    ├── src/
    │   ├── components/           # 公共组件
    │   ├── views/                # 页面视图
    │   ├── router/               # 路由配置
    │   ├── store/                # 状态管理
    │   └── api/                  # API 接口
    ├── vite.config.js            # Vite 配置
    └── package.json              # 依赖配置
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+

### 后端启动

1. **配置数据库**

创建 MySQL 数据库并导入初始化脚本：

```sql
CREATE DATABASE user_center DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
source sql/init.sql
```

2. **配置 Redis**

确保本地 Redis 服务已启动，默认配置：
- 主机：localhost
- 端口：6379

3. **修改配置文件**

编辑 `user-center-backend/src/main/resources/application.yml`，配置数据库和 Redis 连接信息：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_center?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
```

4. **启动后端服务**

```bash
cd user-center-backend
mvn spring-boot:run
```

后端服务启动后，访问 Knife4j API 文档：
- URL: http://localhost:8080/doc.html

### 前端启动

1. **安装依赖**

```bash
cd user-center-frontend
npm install
```

2. **启动开发服务器**

```bash
npm run dev
```

前端服务启动后，访问：
- URL: http://localhost:5173

3. **构建生产版本**

```bash
npm run build
```

构建产物位于 `dist/` 目录。

## API 接口

### 用户相关

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/user/register | 用户注册 |
| POST | /api/user/login | 用户登录 |
| GET | /api/user/{id} | 获取用户信息 |
| PUT | /api/user | 更新用户信息 |
| DELETE | /api/user/{id} | 删除用户 |
| GET | /api/user/list | 分页查询用户 |

### 验证码相关

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/captcha/generate | 生成验证码 |

### 文件上传

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | /api/file/upload | 上传文件 |

## 配置说明

### 开发环境配置

编辑 `application-dev.yml` 配置文件：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/user_center?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: root
  
  redis:
    host: localhost
    port: 6379

mybatis:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.weiki.usercenter.entity
```

### JWT 配置

JWT 密钥和过期时间在配置文件中设置：

```yaml
jwt:
  secret: your-secret-key
  expiration: 86400000  # 24小时（毫秒）
```

## 常见问题

### 1. 启动失败，提示端口被占用

修改 `application.yml` 中的 server.port 配置，更换端口号。

### 2. Redis 连接失败

确认 Redis 服务已启动，检查 redis.host 和 redis.port 配置。

### 3. 数据库连接失败

确认 MySQL 服务已启动，检查数据库用户名、密码和数据库名称。

## 开发指南

### 代码规范

- 遵循阿里巴巴 Java 开发规范
- 使用 Lombok 简化代码
- 采用 RESTful API 设计风格

### 分层架构

```
Controller (控制器层) -> Service (业务层) -> Mapper (数据层)
```

## 许可证

 Apache  License