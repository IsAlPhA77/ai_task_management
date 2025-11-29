package com.scu.ai_task_management.common.constants;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

public class TokenConstants {
    public static final String SECRET_KEY_STRING = "It's my ai_task_management project 123456789!";
    public static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes());
    public static final JwtParser JWT_PARSER = Jwts.parser()
            .verifyWith(SECRET_KEY)
            .build();

    public static final long ACCESS_TOKEN_EXPIRE_TIME_MS = 15 * 60 * 1000;
    public static final long REFRESH_TOKEN_EXPIRE_TIME_MS = 7 * 24 * 60 * 60 * 1000;
    public static final int TOKEN_ALREADY_EXPIRED = 0;

    public static final String REDIS_BLACKLIST_PREFIX = "TOKEN:BLACKLIST:";
    public static final String REFRESH_TOKEN_COOKIE = "refresh_token";
    public static final String COOKIE_PATH = "/";
    public static final int REFRESH_TOKEN_MAX_AGE = 7 * 24 * 3600; // 7 天
    public static final boolean HTTP_ONLY = true;
    public static final boolean SECURE = true; // 生产环境必须开启 HTTPS
    public static final String SAME_SITE = "Lax"; // 防 CSRF：Lax 或 Strict
    public static final String BLACKLIST_VALUE = "1";
    public static final String X_NEW_TOKEN = "X-NEW-TOKEN";
}
