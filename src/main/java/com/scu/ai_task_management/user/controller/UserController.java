package com.scu.ai_task_management.user.controller;

import com.scu.ai_task_management.common.annotation.CurrentUserId;
import com.scu.ai_task_management.common.utils.Result;
import com.scu.ai_task_management.common.utils.CookieTokenUtil;
import com.scu.ai_task_management.user.model.*;
import com.scu.ai_task_management.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理", description = "用户的注册、登录、信息管理接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CookieTokenUtil cookieTokenUtil;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户并自动登录")
    public Result<UserLoginVO> register(@Valid @RequestBody UserRegisterDTO registerDTO, HttpServletResponse response) {
        log.info("接收用户注册请求: {}", registerDTO.getAccount());
        UserLoginVO loginVO = userService.register(registerDTO, response);
        return Result.success("注册成功", loginVO);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录验证")
    public Result<UserLoginVO> login(@Valid @RequestBody UserLoginDTO loginDTO, HttpServletResponse response) {
        log.info("接收用户登录请求: {}", loginDTO.getAccount());
        UserLoginVO loginVO = userService.login(loginDTO, response);
        return Result.success("登录成功", loginVO);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出，将 token 加入黑名单")
    public Result<String> logout(HttpServletRequest request, HttpServletResponse response) {
        log.info("接收用户登出请求");
        userService.logout(request, response);
        return Result.success("登出成功");
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token", description = "使用 refresh token 刷新 access token")
    public Result<String> refresh(HttpServletRequest request, HttpServletResponse response) {
        log.info("接收刷新 Token 请求");
        String newAccessToken = userService.refreshToken(request, response);
        return Result.success("刷新成功", newAccessToken);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserVO> getUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id) {
        log.info("接收获取用户信息请求, ID: {}", id);
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    @GetMapping("/account/{account}")
    @Operation(summary = "根据账户名获取用户信息", description = "根据账户名获取用户详细信息")
    public Result<UserVO> getUserByAccount(
            @Parameter(description = "账户名", required = true, example = "John")
            @PathVariable String account) {
        log.info("接收根据账户名获取用户信息请求, 账户: {}", account);
        UserVO userVO = userService.getUserByAccount(account);
        return Result.success(userVO);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @CurrentUserId Long currentUserId,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        log.info("接收更新用户信息请求, ID: {}, 更新内容: {}", id, updateDTO);
        UserVO userVO = userService.updateUser(currentUserId, id, updateDTO);
        return Result.success("更新成功", userVO);
    }

    @PutMapping("/{id}/password")
    @Operation(summary = "修改密码", description = "修改用户密码")
    public Result<String> changePassword(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable Long id,
            @CurrentUserId Long currentUserId,
            @Valid @RequestBody PasswordChangeDTO passwordDTO) {
        log.info("接收修改密码请求, 用户ID: {}", id);
        userService.changePassword(currentUserId, id, passwordDTO.getOldPassword(), passwordDTO.getNewPassword());
        return Result.success("密码修改成功");
    }

    @GetMapping("/check/account/{account}")
    @Operation(summary = "检查账户是否存在", description = "检查指定账户名是否已被注册")
    public Result<Boolean> checkAccount(
            @Parameter(description = "账户名", required = true, example = "John")
            @PathVariable String account) {
        log.info("接收检查账户请求, 账户: {}", account);
        boolean exists = userService.existsByAccount(account);
        return Result.success(exists);
    }

    @GetMapping("/check/email/{email}")
    @Operation(summary = "检查邮箱是否存在", description = "检查指定邮箱是否已被注册")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱", required = true, example = "zhangsan@example.com")
            @PathVariable String email) {
        log.info("接收检查邮箱请求, 邮箱: {}", email);
        boolean exists = userService.existsByEmail(email);
        return Result.success(exists);
    }
}

