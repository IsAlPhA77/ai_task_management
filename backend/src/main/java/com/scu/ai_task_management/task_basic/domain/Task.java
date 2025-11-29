package com.scu.ai_task_management.task_basic.domain;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("tasks")
public class Task {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 所属用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 任务标题
     */
    @TableField("title")
    private String title;

    /**
     * 任务描述
     */
    @TableField("description")
    private String description;

    /**
     * 任务状态: TODO-待办，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消
     */
    @TableField("status")
    private TaskStatus status = TaskStatus.TODO;

    /**
     * 优先级分数（AI计算）
     */
    @TableField("priority")
    private Integer priority = 0;

    /**
     * 任务分类
     */
    @TableField("category")
    private String category;

    /**
     * 截止时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("deadline")
    private LocalDateTime deadline;

    /**
     * 预估耗时（分钟）
     */
    @TableField("estimated_duration")
    private Integer estimatedDuration;

    /**
     * 实际耗时（分钟）
     */
    @TableField("actual_duration")
    private Integer actualDuration;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("start_time")
    private LocalDateTime startTime;

    /**
     * 完成时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * 依赖任务ID列表（JSON格式）
     */
    @TableField(value = "dependencies", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<Long> dependencies;

    /**
     * 标签列表（JSON格式）
     */
    @TableField(value = "tags", typeHandler = com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler.class)
    private List<String> tags;

    /**
     * 自然语言原始输入
     */
    @TableField("original_input")
    private String originalInput;

    /**
     * 是否为模板
     */
    @TableField("is_template")
    private Boolean isTemplate = false;

    /**
     * 是否删除（软删除）
     */
    @TableLogic
    @TableField("is_deleted")
    private Boolean isDeleted = false;

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

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        TODO("待办"),
        IN_PROGRESS("进行中"),
        COMPLETED("已完成"),
        CANCELLED("已取消");

        private final String description;

        TaskStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}


