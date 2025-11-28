package com.scu.ai_task_management.infra.web;

import com.scu.ai_task_management.user.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;


/**
 * Web配置：管理拦截器
 * 认证策略：所有请求默认需要认证，白名单路径除外
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Autowired
    private CurrentUserIdArgumentResolver currentUserIdArgumentResolver;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")  // 拦截所有请求
                .excludePathPatterns(getWhitelistPaths()); // 白名单路径

    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserIdArgumentResolver);
    }

    /**
     * 白名单路径配置
     * 这些路径不需要Token认证
     */
    private String[] getWhitelistPaths() {
        return new String[]{
                // 认证相关接口
                "/api/user/login",
                "/api/user/register",
                "/api/user/refresh",
                // API文档
                "/swagger-ui.html",
                "/swagger-ui/**",
                "/v3/api-docs/**",
        };
    }


}
