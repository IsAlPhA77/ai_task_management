package com.scu.ai_task_management.infra.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 关闭 CSRF 防护
                .csrf(csrf -> csrf.disable())

                // 关闭 CORS（如果需要跨域，单独配置CorsConfig）
                .cors(cors -> cors.disable())

                // 完全放行所有请求，由自定义拦截器处理认证
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )

                // 禁用默认登录页、HTTP Basic、Session
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .sessionManagement(session -> session.disable());

        return http.build();
    }
}