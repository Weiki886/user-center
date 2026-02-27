package com.weiki.usercenter.service.impl;

import com.weiki.usercenter.dto.*;
import com.weiki.usercenter.entity.PageVO;
import com.weiki.usercenter.entity.User;
import com.weiki.usercenter.exception.BusinessException;
import com.weiki.usercenter.mapper.UserMapper;
import com.weiki.usercenter.service.UserService;
import com.weiki.usercenter.service.RedisTokenService;
import com.weiki.usercenter.service.RedisRateLimitService;
import com.weiki.usercenter.service.RedisCaptchaService;
import com.weiki.usercenter.utils.SecurityUtils;
import cn.hutool.crypto.SecureUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final RedisTokenService redisTokenService;
    private final RedisRateLimitService redisRateLimitService;
    private final RedisCaptchaService redisCaptchaService;

    public UserServiceImpl(UserMapper userMapper,
                          RedisTokenService redisTokenService,
                          RedisRateLimitService redisRateLimitService,
                          RedisCaptchaService redisCaptchaService) {
        this.userMapper = userMapper;
        this.redisTokenService = redisTokenService;
        this.redisRateLimitService = redisRateLimitService;
        this.redisCaptchaService = redisCaptchaService;
    }
    
    @Override
    @Transactional(transactionManager = "transactionManager")
    public Long register(UserRegisterDTO registerDTO) {
        log.info("开始处理用户注册，账号：{}", registerDTO.getUserAccount());

        // 1. 验证验证码（只有当 captchaId 不为空时才验证）
        if (registerDTO.getCaptchaId() != null && !registerDTO.getCaptchaId().isEmpty()) {
            if (!redisCaptchaService.verifyCaptcha(registerDTO.getCaptchaId(), registerDTO.getCaptchaCode())) {
                log.warn("注册失败，验证码错误：{}", registerDTO.getCaptchaId());
                throw new BusinessException("验证码错误");
            }
        } else {
            // 没有验证码ID时不验证（避免验证码还没加载完的情况）
            log.warn("注册失败，验证码ID为空");
            throw new BusinessException("请先获取验证码");
        }

        // 2. 检查账号是否已存在（未删除）
        User existUser = userMapper.selectByAccount(registerDTO.getUserAccount());
        if (existUser != null) {
            log.warn("注册失败，账号已存在：{}", registerDTO.getUserAccount());
            throw new BusinessException("账号已存在");
        }
        
        // 3. 检查是否有已删除的相同账号，如果有则恢复
        User deletedUser = userMapper.selectByAccountIncludeDeleted(registerDTO.getUserAccount());
        if (deletedUser != null) {
            log.info("发现已删除的账号，正在恢复：{}", registerDTO.getUserAccount());
            // 更新已删除的用户信息
            String encryptedPassword = SecureUtil.md5(registerDTO.getUserPassword() + "user_center");
            User user = User.builder()
                    .id(deletedUser.getId())
                    .username(registerDTO.getUsername())
                    .userPassword(encryptedPassword)
                    .gender(registerDTO.getGender())
                    .phone(registerDTO.getPhone())
                    .email(registerDTO.getEmail())
                    .avatarUrl(registerDTO.getAvatarUrl())
                    .userRole("user")
                    .build();
            userMapper.recoverAndUpdate(user);

            log.info("用户恢复成功，账号：{}，用户ID：{}",
                    registerDTO.getUserAccount(), deletedUser.getId());

            return deletedUser.getId();
        }

        // 4. 检查两次密码是否一致
        if (!registerDTO.getUserPassword().equals(registerDTO.getCheckPassword())) {
            log.warn("注册失败，两次密码不一致");
            throw new BusinessException("两次密码不一致");
        }
        
        // 4. 密码加密（使用Hutool加盐MD5）
        String encryptedPassword = SecureUtil.md5(registerDTO.getUserPassword() + "user_center");
        
        // 5. 创建用户对象
        User user = User.builder()
                .username(registerDTO.getUsername())
                .userAccount(registerDTO.getUserAccount())
                .userPassword(encryptedPassword)
                .gender(registerDTO.getGender())
                .phone(registerDTO.getPhone())
                .email(registerDTO.getEmail())
                .avatarUrl(registerDTO.getAvatarUrl())
                .userRole("user")
                .isDelete(0)
                .build();
        
        // 6. 插入数据库
        int rows = userMapper.insert(user);
        if (rows <= 0) {
            throw new BusinessException("注册失败，请稍后重试");
        }
        
        log.info("用户注册成功，账号：{}，用户ID：{}", 
                registerDTO.getUserAccount(), user.getId());
        
        return user.getId();
    }
    
    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        log.info("开始处理用户登录，账号：{}", loginDTO.getUserAccount());

        // 1. 检查登录是否被锁定（限流）
        if (!redisRateLimitService.isLoginAllowed(loginDTO.getUserAccount())) {
            long ttl = redisRateLimitService.getTTL("login:fail:" + loginDTO.getUserAccount());
            throw new BusinessException("登录失败次数过多，请" + (ttl / 60) + "分钟后重试");
        }

        // 2. 根据账号查询用户
        User user = userMapper.selectByAccount(loginDTO.getUserAccount());
        if (user == null) {
            // 记录登录失败
            redisRateLimitService.recordLoginFailure(loginDTO.getUserAccount());
            log.warn("登录失败，用户不存在：{}", loginDTO.getUserAccount());
            throw new BusinessException("账号或密码错误");
        }

        // 3. 检查用户是否已删除
        if (user.getIsDelete() == 1) {
            log.warn("登录失败，用户已被删除：{}", loginDTO.getUserAccount());
            throw new BusinessException("账号或密码错误");
        }

        // 4. 验证密码（使用Hutool加盐MD5）
        String encryptedPassword = SecureUtil.md5(loginDTO.getUserPassword() + "user_center");
        if (!encryptedPassword.equals(user.getUserPassword())) {
            // 记录登录失败
            redisRateLimitService.recordLoginFailure(loginDTO.getUserAccount());
            log.warn("登录失败，密码错误：{}", loginDTO.getUserAccount());
            throw new BusinessException("账号或密码错误");
        }

        // 5. 生成Token
        String token = "token-" + user.getId() + "-" + System.currentTimeMillis();

        // 6. 将Token存储到Redis（支持分布式环境和主动登出）
        redisTokenService.saveToken(token, user);

        // 7. 清除登录失败记录
        redisRateLimitService.clearLoginFailures(loginDTO.getUserAccount());

        // 8. 将用户信息存入Session（用于权限校验）
        SecurityUtils.setCurrentUser(user);

        // 9. 转换为VO
        UserVO userVO = convertToVO(user);

        log.info("用户登录成功，账号：{}，用户ID：{}",
                loginDTO.getUserAccount(), user.getId());

        // 10. 返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUser(userVO);
        return loginVO;
    }
    
    @Override
    public void logout(Long userId) {
        // 权限校验：检查当前登录用户是否有权限登出该账户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        // 普通用户只能登出自己的账号，管理员可以登出任意用户账号
        if (!"admin".equals(currentUser.getUserRole()) && !currentUser.getId().equals(userId)) {
            throw new BusinessException("没有权限登出其他用户");
        }

        log.info("用户登出，用户ID：{}", userId);
        
        // 登出逻辑
        if (currentUser.getId().equals(userId)) {
            // 自己登出：删除当前Token
            String token = SecurityUtils.getToken();
            if (token != null) {
                redisTokenService.deleteToken(token, userId);
            }
        } else {
            // 管理员强制登出其他用户：删除该用户的所有Token
            redisTokenService.deleteUserTokens(userId);
        }
        
        // 清除Session中的用户信息
        SecurityUtils.setCurrentUser(null);
    }
    
    @Override
    public UserVO getUserById(Long id) {
        // 权限校验：检查当前登录用户是否有权限查看该用户信息
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        // 普通用户只能查看自己的信息，管理员可以查看所有用户信息
        if (!"admin".equals(currentUser.getUserRole()) && !currentUser.getId().equals(id)) {
            throw new BusinessException("没有权限查看其他用户信息");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return convertToVO(user);
    }
    
    @Override
    public List<UserVO> getAllUsers() {
        // 权限校验：只有管理员可以查看所有用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        if (!"admin".equals(currentUser.getUserRole())) {
            throw new BusinessException("没有权限查看所有用户");
        }

        List<User> users = userMapper.selectAll();
        List<UserVO> userVOs = new ArrayList<>();
        for (User user : users) {
            userVOs.add(convertToVO(user));
        }
        return userVOs;
    }
    
    @Override
    public PageVO<UserVO> getUsersByPage(UserQueryDTO queryDTO) {
        // 权限校验：只有管理员可以分页查看用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        if (!"admin".equals(currentUser.getUserRole())) {
            throw new BusinessException("没有权限查看用户列表");
        }

        int offset = (queryDTO.getPage() - 1) * queryDTO.getSize();
        List<User> users = userMapper.selectByPage(queryDTO.getUsername(), offset, queryDTO.getSize());
        List<UserVO> userVOs = new ArrayList<>();
        for (User user : users) {
            userVOs.add(convertToVO(user));
        }
        int total = userMapper.count(queryDTO.getUsername());
        return PageVO.<UserVO>builder()
                .page(queryDTO.getPage())
                .size(queryDTO.getSize())
                .total((long) total)
                .pages((int) Math.ceil((double) total / queryDTO.getSize()))
                .records(userVOs)
                .build();
    }
    
    @Override
    public List<UserVO> searchUsers(String username) {
        // 权限校验：只有管理员可以搜索用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        if (!"admin".equals(currentUser.getUserRole())) {
            throw new BusinessException("没有权限搜索用户");
        }

        List<User> users = userMapper.selectByUsername(username);
        List<UserVO> userVOs = new ArrayList<>();
        for (User user : users) {
            userVOs.add(convertToVO(user));
        }
        return userVOs;
    }
    
    @Override
    public User getUserByAccount(String account) {
        return userMapper.selectByAccount(account);
    }
    
    @Override
    public boolean updateUser(Long id, UserUpdateDTO updateDTO) {
        // 权限校验：检查当前登录用户是否有权限修改该用户信息
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        // 普通用户只能修改自己的信息，管理员可以修改任意用户信息
        if (!"admin".equals(currentUser.getUserRole()) && !currentUser.getId().equals(id)) {
            throw new BusinessException("没有权限修改其他用户信息");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        User updateUser = new User();
        updateUser.setId(id);
        BeanUtils.copyProperties(updateDTO, updateUser);
        int rows = userMapper.update(updateUser);
        return rows > 0;
    }
    
    @Override
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        // 权限校验：检查当前登录用户是否有权限修改密码
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        // 普通用户只能修改自己的密码，管理员可以修改任意用户密码
        if (!"admin".equals(currentUser.getUserRole()) && !currentUser.getId().equals(id)) {
            throw new BusinessException("没有权限修改其他用户的密码");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String encryptedOldPassword = SecureUtil.md5(oldPassword + "user_center");
        if (!encryptedOldPassword.equals(user.getUserPassword())) {
            throw new BusinessException("旧密码错误");
        }
        String encryptedNewPassword = SecureUtil.md5(newPassword + "user_center");
        int rows = userMapper.updatePasswordById(id, encryptedNewPassword);
        return rows > 0;
    }
    
    @Override
    public boolean resetPassword(Long id, String newPassword) {
        // 权限校验：只有管理员可以重置用户密码
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        if (!"admin".equals(currentUser.getUserRole())) {
            throw new BusinessException("没有权限重置用户密码");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 管理员重置密码不需要验证旧密码
        String encryptedNewPassword = SecureUtil.md5(newPassword + "user_center");
        int rows = userMapper.updatePasswordById(id, encryptedNewPassword);
        log.info("管理员重置用户密码，用户ID：{}", id);
        return rows > 0;
    }
    
    @Override
    public boolean deleteUser(Long id) {
        // 权限校验：检查当前登录用户是否有权限删除该用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        // 普通用户只能删除自己的账号，管理员可以删除任意用户账号
        if (!"admin".equals(currentUser.getUserRole()) && !currentUser.getId().equals(id)) {
            throw new BusinessException("没有权限删除其他用户");
        }

        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        int rows = userMapper.logicalDeleteById(id);
        return rows > 0;
    }
    
    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        if (user.getCreateTime() != null) {
            vo.setCreateTime(user.getCreateTime().toString());
        }
        return vo;
    }

    @Override
    public UserVO getCurrentUser() {
        // 从 SecurityUtils 获取当前登录用户
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            throw new BusinessException("用户未登录");
        }
        return convertToVO(currentUser);
    }
}


