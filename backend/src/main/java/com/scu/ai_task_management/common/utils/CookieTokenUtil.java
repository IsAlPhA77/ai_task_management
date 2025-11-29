package com.scu.ai_task_management.common.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

import static com.scu.ai_task_management.common.constants.TokenConstants.*;


/**
 * CookieTokenUtil: 统一管理 AccessToken 和 RefreshToken 的读取与设置
 * - AccessToken: 从 Header 获取
 * - RefreshToken: 从 HttpOnly Cookie 获取/设置
 */
@Component
public class CookieTokenUtil {

    public Optional<String> getAccessToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");

        // 检查 header 是否存在且格式正确
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7).trim();
            // 确保提取的 token 不为空
            return token.isEmpty() ? Optional.empty() : Optional.of(token);
        }

        return Optional.empty();
    }

    public Optional<String> getRefreshToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isEmpty()) // 过滤空值
                .findFirst();
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, token)
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(REFRESH_TOKEN_MAX_AGE)
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(HTTP_ONLY)
                .secure(SECURE)
                .path(COOKIE_PATH)
                .maxAge(0)
                .sameSite(SAME_SITE)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
