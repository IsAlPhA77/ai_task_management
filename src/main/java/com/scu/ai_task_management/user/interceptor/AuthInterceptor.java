package com.scu.ai_task_management.user.interceptor;

import com.scu.ai_task_management.common.utils.CookieTokenUtil;
import com.scu.ai_task_management.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Optional;

import static com.scu.ai_task_management.common.constants.TokenConstants.*;

/**
 * AuthInterceptor: 负责用户认证和黑名单校验
 * 注意：由于Spring Security已配置为permitAll，所有认证逻辑由此拦截器负责
 */
@Component
@Slf4j
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private CookieTokenUtil cookieTokenUtil;

    // 白名单路径（与 WebConfig 保持一致，作为双重保险）
    private static final String[] WHITELIST_PATHS = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh",
            "/swagger-ui.html",
            "/swagger-ui",
            "/v3/api-docs"
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        
        log.info("拦截器处理请求: {} {}, Handler: {}", method, requestURI, handler.getClass().getSimpleName());

        // 双重保险：检查是否在白名单中（理论上不应该到达这里，因为 WebConfig 已经配置了白名单）
        for (String whitelistPath : WHITELIST_PATHS) {
            if (requestURI.equals(whitelistPath) || requestURI.startsWith(whitelistPath + "/")) {
                log.debug("请求在白名单中，跳过认证: {}", requestURI);
                return true;
            }
        }
        
        // 检查 Swagger 相关路径
        if (requestURI.startsWith("/swagger-ui") || requestURI.startsWith("/v3/api-docs")) {
            log.debug("Swagger 请求，跳过认证: {}", requestURI);
            return true;
        }

        try {
            // 1. 获取 Token
            Optional<String> tokenOpt = cookieTokenUtil.getAccessToken(request);
            if (tokenOpt.isEmpty()) {
                log.warn("请求缺少认证信息: {} {}", method, requestURI);
                return sendError(response, "缺少认证信息");
            }

            String accessToken = tokenOpt.get();

            // 2. 提取 JTI
            String jti = jwtUtil.getJtiFromToken(accessToken);
            if (jti == null) {
                return sendError(response, "无效的Token结构");
            }

            // 3. 检查黑名单(优先级最高)
            String blacklistKey = REDIS_BLACKLIST_PREFIX + jti;
            if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(blacklistKey))) {
                return sendError(response, "Token已失效,请重新登录");
            }

            // 4. 检查过期
            if (jwtUtil.isTokenExpired(accessToken)) {
                return sendError(response, "Token已过期");
            }

            // 5. 提取用户信息并存入 request
            Long userId = jwtUtil.getUserIdFromToken(accessToken);
            request.setAttribute("userId", userId);
            request.setAttribute("jti", jti);

            return true;

        } catch (Exception e) {
            log.error("Token验证失败: URI={}, Error={}", request.getRequestURI(), e.getMessage());
            return sendError(response, "认证失败");
        }
    }

    /**
     * 统一错误响应
     */
    private boolean sendError(HttpServletResponse response, String message) throws Exception {
        response.setStatus(401);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format("{\"code\":401,\"message\":\"%s\"}", message));
        return false;
    }
}
