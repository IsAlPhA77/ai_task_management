package com.scu.ai_task_management.task_query.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 任务历史记录实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("task_history")
public class TaskHistory {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 任务ID
     */
    @TableField("task_id")
    private Long taskId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 操作类型
     */
    @TableField("action")
    private ActionType action;

    /**
     * 变更前的值（JSON格式）
     */
    @TableField(value = "old_value", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> oldValue;

    /**
     * 变更后的值（JSON格式）
     */
    @TableField(value = "new_value", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private Map<String, Object> newValue;

    /**
     * 变更字段名
     */
    @TableField("field_name")
    private String fieldName;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 操作类型枚举
     */
    public enum ActionType {
        CREATE("创建"),
        UPDATE("更新"),
        STATUS_CHANGE("状态变更"),
        DELETE("删除");

        private final String description;

        ActionType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}


