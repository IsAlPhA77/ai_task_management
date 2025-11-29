package com.scu.ai_task_management.user.domain;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.Optional;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据账户名查询用户
     */
    default Optional<User> findByAccount(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        User user = selectOne(wrapper);
        return Optional.ofNullable(user);
    }

    /**
     * 根据邮箱查询用户
     */
    default Optional<User> findByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        User user = selectOne(wrapper);
        return Optional.ofNullable(user);
    }

    /**
     * 检查账户是否存在
     */
    default boolean existsByAccount(String account) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getAccount, account);
        return selectCount(wrapper) > 0;
    }

    /**
     * 检查邮箱是否存在
     */
    default boolean existsByEmail(String email) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, email);
        return selectCount(wrapper) > 0;
    }
}


