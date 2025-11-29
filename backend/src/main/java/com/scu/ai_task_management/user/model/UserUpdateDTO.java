package com.scu.ai_task_management.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息更新DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "更新用户信息请求")
public class UserUpdateDTO {

    @Size(max = 100, message = "昵称长度不能超过100个字符")
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    private String email;

    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    @Schema(description = "新密码", example = "123456")
    private String password;
}

