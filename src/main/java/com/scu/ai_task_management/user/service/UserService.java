package com.scu.ai_task_management.user.service;

import com.scu.ai_task_management.user.model.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {
    /**
     * 用户注册（注册后自动登录）
     */
    UserLoginVO register(UserRegisterDTO registerDTO, HttpServletResponse response);

    /**
     * 用户登录
     */
    UserLoginVO login(UserLoginDTO loginDTO, HttpServletResponse response);

    /**
     * 根据ID获取用户信息
     */
    UserVO getUserById(Long id);

    /**
     * 根据账户名获取用户信息
     */
    UserVO getUserByAccount(String account);

    /**
     * 更新用户信息
     */
    UserVO updateUser(Long operatorUserId, Long id, UserUpdateDTO updateDTO);

    /**
     * 修改密码
     */
    void changePassword(Long operatorUserId, Long id, String oldPassword, String newPassword);

    /**
     * 检查账户是否存在
     */
    boolean existsByAccount(String account);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 用户登出
     */
    void logout(HttpServletRequest request, HttpServletResponse response);

    /**
     * 刷新 Access Token
     */
    String refreshToken(HttpServletRequest request, HttpServletResponse response);
}

