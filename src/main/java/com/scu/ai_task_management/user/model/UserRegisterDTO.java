package com.scu.ai_task_management.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户注册DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户注册请求")
public class UserRegisterDTO {

    @NotBlank(message = "账户名不能为空")
    @Size(min = 3, max = 50, message = "账户名长度必须在3-50个字符之间")
    @Schema(description = "账户名", example = "zhangsan", required = true)
    private String account;

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "密码", example = "123456", required = true)
    private String password;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱地址", example = "zhangsan@example.com", required = true)
    private String email;
}

