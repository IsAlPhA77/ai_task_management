package com.scu.ai_task_management.boot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Spring Boot应用启动类
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.scu.ai_task_management")
@MapperScan("com.scu.ai_task_management.**.domain")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
