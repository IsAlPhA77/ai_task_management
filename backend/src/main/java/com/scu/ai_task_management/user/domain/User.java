package com.scu.ai_task_management.user.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户名
     */
    @TableField("account")
    private String account;

    /**
     * 用户昵称
     */
    @TableField("nickname")
    private String nickname;

    /**
     * 加密后的密码
     */
    @TableField("password")
    private String password;

    /**
     * 邮箱地址
     */
    @TableField("email")
    private String email;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}


