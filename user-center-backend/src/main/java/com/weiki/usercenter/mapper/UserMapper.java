package com.weiki.usercenter.mapper;

import com.weiki.usercenter.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper {

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 根据ID查询用户
     */
    User selectById(Long id);

    /**
     * 根据账号查询用户
     */
    User selectByAccount(String userAccount);

    /**
     * 根据账号查询用户（包括已删除的，用于注册时恢复）
     */
    User selectByAccountIncludeDeleted(String userAccount);

    /**
     * 恢复并更新已删除的用户信息
     */
    int recoverAndUpdate(User user);

    /**
     * 查询所有用户
     */
    List<User> selectAll();

    /**
     * 根据用户名模糊查询
     */
    List<User> selectByUsername(@Param("username") String username);

    /**
     * 分页查询用户
     */
    List<User> selectByPage(@Param("username") String username, @Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 更新用户
     */
    int update(User user);

    /**
     * 根据ID更新密码
     */
    int updatePasswordById(@Param("id") Long id, @Param("newPassword") String newPassword);

    /**
     * 根据ID逻辑删除用户
     */
    int logicalDeleteById(Long id);

    /**
     * 统计用户数量
     */
    int count(@Param("username") String username);
}

