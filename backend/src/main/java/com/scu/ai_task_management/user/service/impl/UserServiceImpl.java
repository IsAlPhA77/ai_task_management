package com.scu.ai_task_management.user.service.impl;

import com.scu.ai_task_management.user.domain.User;
import com.scu.ai_task_management.user.domain.UserMapper;
import com.scu.ai_task_management.user.model.*;
import com.scu.ai_task_management.user.service.UserService;
import com.scu.ai_task_management.common.exception.UserException;
import com.scu.ai_task_management.common.utils.JwtUtil;
import com.scu.ai_task_management.common.utils.CookieTokenUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.scu.ai_task_management.common.constants.TokenConstants.*;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CookieTokenUtil cookieTokenUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public UserLoginVO register(UserRegisterDTO registerDTO, HttpServletResponse response) {
        log.info("用户注册: {}", registerDTO.getAccount());

        // 检查账户是否已存在
        if (userMapper.existsByAccount(registerDTO.getAccount())) {
            throw UserException.userAlreadyExists("账户名");
        }

        // 检查邮箱是否已存在
        if (userMapper.existsByEmail(registerDTO.getEmail())) {
            throw UserException.userAlreadyExists("邮箱");
        }

        // 构建User实体
        User user = User.builder()
                .account(registerDTO.getAccount())
                .nickname(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getAccount())
                .email(registerDTO.getEmail())
                .password(passwordEncoder.encode(registerDTO.getPassword()))
                .build();

        // 保存用户
        userMapper.insert(user);

        log.info("用户注册成功，ID: {}, 账户: {}", user.getId(), user.getAccount());

        // 生成 token 并自动登录
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount());

        // 将 refresh token 存入 Cookie
        cookieTokenUtil.setRefreshTokenCookie(response, refreshToken);

        // 构建登录响应
        return UserLoginVO.builder()
                .user(UserVO.fromEntity(user))
                .accessToken(accessToken)
                .build();
    }

    @Override
    public UserLoginVO login(UserLoginDTO loginDTO, HttpServletResponse response) {
        log.info("用户登录: {}", loginDTO.getAccount());

        // 根据账户名或邮箱查询用户
        User user = userMapper.findByAccount(loginDTO.getAccount())
                .orElseGet(() -> userMapper.findByEmail(loginDTO.getAccount())
                        .orElseThrow(UserException::accountOrPasswordError));

        // 验证密码
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw UserException.accountOrPasswordError();
        }

        log.info("用户登录成功，ID: {}, 账户: {}", user.getId(), user.getAccount());

        // 生成 token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getAccount());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getAccount());

        // 将 refresh token 存入 Cookie
        cookieTokenUtil.setRefreshTokenCookie(response, refreshToken);

        // 构建登录响应
        return UserLoginVO.builder()
                .user(UserVO.fromEntity(user))
                .accessToken(accessToken)
                .build();
    }

    @Override
    public UserVO getUserById(Long id) {
        log.info("查询用户，ID: {}", id);
        User user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound();
        }
        return UserVO.fromEntity(user);
    }

    @Override
    public UserVO getUserByAccount(String account) {
        log.info("查询用户，账户: {}", account);
        User user = userMapper.findByAccount(account)
                .orElseThrow(UserException::userNotFound);
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional
    public UserVO updateUser(Long operatorUserId, Long id, UserUpdateDTO updateDTO) {
        log.info("更新用户信息，操作者ID: {}, 目标ID: {}, 更新内容: {}", operatorUserId, id, updateDTO);

        if (!id.equals(operatorUserId)) {
            throw UserException.noPermission();
        }

        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound();
        }

        // 更新昵称
        if (updateDTO.getNickname() != null && !updateDTO.getNickname().equals(user.getNickname())) {
            user.setNickname(updateDTO.getNickname());
        }

        // 更新邮箱
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            // 检查新邮箱是否已被其他用户使用
            User existingUser = userMapper.findByEmail(updateDTO.getEmail()).orElse(null);
            if (existingUser != null && !existingUser.getId().equals(id)) {
                throw UserException.userAlreadyExists("邮箱");
            }
            user.setEmail(updateDTO.getEmail());
        }

        // 更新密码
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
        }

        // 保存更新
        userMapper.updateById(user);

        log.info("用户信息更新成功，ID: {}", user.getId());
        return UserVO.fromEntity(user);
    }

    @Override
    @Transactional
    public void changePassword(Long operatorUserId, Long id, String oldPassword, String newPassword) {
        log.info("修改密码，操作者ID: {}, 目标ID: {}", operatorUserId, id);

        if (!id.equals(operatorUserId)) {
            throw UserException.noPermission();
        }

        // 查询用户
        User user = userMapper.selectById(id);
        if (user == null) {
            throw UserException.userNotFound();
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw UserException.wrongPassword();
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);

        log.info("密码修改成功，用户ID: {}", id);
    }

    @Override
    public boolean existsByAccount(String account) {
        return userMapper.existsByAccount(account);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userMapper.existsByEmail(email);
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("用户登出");

        // 获取 access token
        Optional<String> accessTokenOpt = cookieTokenUtil.getAccessToken(request);
        if (accessTokenOpt.isPresent()) {
            String accessToken = accessTokenOpt.get();
            String jti = jwtUtil.getJtiFromToken(accessToken);

            if (jti != null) {
                // 将 access token 加入黑名单
                String blacklistKey = REDIS_BLACKLIST_PREFIX + jti;
                long remainingTime = jwtUtil.getRemainingTimeFromToken(accessToken);
                if (remainingTime > 0) {
                    // 设置过期时间为 token 的剩余有效时间
                    stringRedisTemplate.opsForValue().set(blacklistKey, BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
                    log.info("Access token 已加入黑名单，JTI: {}", jti);
                }
            }
        }

        // 清除 refresh token cookie
        cookieTokenUtil.clearRefreshTokenCookie(response);
        log.info("用户登出成功");
    }

    @Override
    public String refreshToken(HttpServletRequest request, HttpServletResponse response) {
        log.info("刷新 Token");

        // 从 Cookie 获取 refresh token
        Optional<String> refreshTokenOpt = cookieTokenUtil.getRefreshToken(request);
        if (refreshTokenOpt.isEmpty()) {
            throw UserException.tokenInvalid();
        }

        String refreshToken = refreshTokenOpt.get();

        // 验证 refresh token
        Claims claims = jwtUtil.parseToken(refreshToken);
        if (claims == null) {
            throw UserException.tokenInvalid();
        }

        // 检查 refresh token 是否在黑名单中
        String refreshJti = claims.getId();
        if (refreshJti != null) {
            String refreshBlacklistKey = REDIS_BLACKLIST_PREFIX + "REFRESH:" + refreshJti;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(refreshBlacklistKey))) {
                throw UserException.tokenInvalid();
            }
        }

        // 检查 refresh token 是否过期
        if (jwtUtil.isTokenExpired(refreshToken)) {
            throw UserException.tokenInvalid();
        }

        // 从 refresh token 中提取用户信息
        Long userId = claims.get("userId", Long.class);
        String account = claims.getSubject();

        if (userId == null || account == null) {
            throw UserException.tokenInvalid();
        }

        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null || !user.getAccount().equals(account)) {
            throw UserException.tokenInvalid();
        }

        // 生成新的 access token 和 refresh token
        String newAccessToken = jwtUtil.generateAccessToken(userId, account);
        String newRefreshToken = jwtUtil.generateRefreshToken(userId, account);

        // 将旧的 refresh token 加入黑名单（可选，增强安全性）
        String oldJti = claims.getId();
        if (oldJti != null) {
            String blacklistKey = REDIS_BLACKLIST_PREFIX + "REFRESH:" + oldJti;
            long remainingTime = jwtUtil.getRemainingTimeFromToken(refreshToken);
            if (remainingTime > 0) {
                stringRedisTemplate.opsForValue().set(blacklistKey, BLACKLIST_VALUE, remainingTime, TimeUnit.MILLISECONDS);
            }
        }

        // 更新 refresh token cookie
        cookieTokenUtil.setRefreshTokenCookie(response, newRefreshToken);

        log.info("Token 刷新成功，用户ID: {}, 账户: {}", userId, account);
        return newAccessToken;
    }
}

