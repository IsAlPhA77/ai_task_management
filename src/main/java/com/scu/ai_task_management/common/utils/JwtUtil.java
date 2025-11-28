package com.scu.ai_task_management.common.utils;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

import static com.scu.ai_task_management.common.constants.TokenConstants.*;

@Slf4j
@Component
public class JwtUtil {

    /**
     * 生成 JWT Access Token
     *
     * @param userId  用户ID
     * @param account 账户名
     * @return JWT Token字符串
     */
    public String generateAccessToken(Long userId, String account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_EXPIRE_TIME_MS);

        return Jwts.builder()
                .subject(account)
                .claim("userId", userId)
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 生成 JWT Refresh Token
     *
     * @param userId  用户ID
     * @return JWT Token字符串
     */
    /**
     * 生成 RefreshToken
     * 包含: userId, account(subject)
     */
    public String generateRefreshToken(Long userId, String account) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_EXPIRE_TIME_MS);

        return Jwts.builder()
                .subject(account)           // ← 统一:subject 存储账号
                .claim("userId", userId)    // ← 统一:自定义字段存储 userId
                .id(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * 验证并解析 Token
     *
     * @param token JWT Token
     * @return Claims对象，解析失败返回null
     */
    public Claims parseToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空");
            return null;
        }

        try {
            return JWT_PARSER.parseSignedClaims(token).getPayload();
        } catch (ExpiredJwtException e) {
            log.warn("Token已过期: {}", e.getMessage());
            return null;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的Token格式: {}", e.getMessage());
            return null;
        } catch (MalformedJwtException e) {
            log.error("Token格式错误: {}", e.getMessage());
            return null;
        } catch (SecurityException e) {
            log.error("Token签名验证失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            log.error("Token参数异常: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            log.error("Token解析失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查 token 是否过期
     *
     * @param token JWT Token
     * @return true表示已过期或无效
     */
    public boolean isTokenExpired(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return true;
        }

        Date expiration = claims.getExpiration();
        return expiration == null || expiration.before(new Date());
    }

    /**
     * 检查 token 是否有效（未过期且格式正确）
     *
     * @param token JWT Token
     * @return true表示有效
     */
    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }

    /**
     * 从 token 中提取账户
     *
     * @param token JWT Token
     * @return 账户名，提取失败返回null
     */
    public String getAccountFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从 token 中提取用户ID
     *
     * @param token JWT Token
     * @return 用户ID，提取失败返回null
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Long.class) : null;
    }

    /**
     * 从 token 中提取角色
     *
     * @param token JWT Token
     * @return 角色，提取失败返回null
     */
    public String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("role", String.class) : null;
    }

    /**
     * 从 token 中获取剩余时间
     *
     * @param token JWT Token
     * @return 剩余时间，提取失败返回null
     */
    public long getRemainingTimeFromToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null || claims.getExpiration() == null) {
            return 0;
        }
        return claims.getExpiration().getTime() - System.currentTimeMillis();
    }

    /**
     * 从 token 中获取唯一id
     *
     * @param token JWT Token
     * @return 唯一id，提取失败返回null
     */
    public String getJtiFromToken(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getId() : null;
    }

    public boolean willExpireSoon(String token, long seconds) {
        Claims claims = parseToken(token);
        Date expiration = claims.getExpiration();
        return expiration != null && (expiration.getTime() - System.currentTimeMillis()) < seconds * 1000;
    }
}