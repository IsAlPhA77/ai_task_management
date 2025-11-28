package com.scu.ai_task_management.user.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.scu.ai_task_management.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户信息VO（返回给前端，不包含密码）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户信息")
public class UserVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "账户名", example = "zhangsan")
    private String account;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "邮箱地址", example = "zhangsan@example.com")
    private String email;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间", example = "2025-11-20 10:00:00")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新时间", example = "2025-11-26 09:00:00")
    private LocalDateTime updatedAt;

    /**
     * 从User实体转换为UserVO
     */
    public static UserVO fromEntity(User user) {
        if (user == null) {
            return null;
        }

        return UserVO.builder()
                .id(user.getId())
                .account(user.getAccount())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

