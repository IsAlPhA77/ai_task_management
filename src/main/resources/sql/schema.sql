-- ====================================
-- 1. 用户表
-- ====================================
CREATE TABLE `users` (
  `id` bigint NOT NULL COMMENT '主键ID，由MyBatis-Plus雪花算法生成',
  `account` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(100) DEFAULT NULL COMMENT '用户昵称',
  `password` varchar(100) NOT NULL COMMENT '加密后的密码',
  `email` varchar(100) NOT NULL COMMENT '邮箱地址',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`account`),
  UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- ====================================
-- 2. 任务表
-- ====================================
CREATE TABLE `tasks` (
    `id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '所属用户ID',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题',
    `description` TEXT COMMENT '任务描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT '任务状态：TODO-待办，IN_PROGRESS-进行中，COMPLETED-已完成，CANCELLED-已取消',
    `priority` INT DEFAULT 0 COMMENT '优先级分数（AI计算，分数越高优先级越高）',
    `category` VARCHAR(50) COMMENT '任务分类（开发/测试/会议/学习等）',
    `deadline` DATETIME COMMENT '截止时间',
    `estimated_duration` INT COMMENT '预估耗时（分钟）',
    `actual_duration` INT COMMENT '实际耗时（分钟）',
    `start_time` DATETIME COMMENT '开始时间',
    `completed_at` DATETIME COMMENT '完成时间',
    `dependencies` JSON COMMENT '依赖任务ID列表，格式：[1,2,3]',
    `tags` JSON COMMENT '标签列表，格式：["紧急","重要"]',
    `original_input` TEXT COMMENT '自然语言原始输入',
    `is_template` TINYINT DEFAULT 0 COMMENT '是否为模板：0-否，1-是',
    `is_deleted` TINYINT DEFAULT 0 COMMENT '是否删除：0-否，1-是',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_status (`status`),
    INDEX idx_deadline (`deadline`),
    INDEX idx_priority (`priority`),
    INDEX idx_category (`category`),
    INDEX idx_is_deleted (`is_deleted`),
    INDEX idx_created_at (`created_at`),
    FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- ====================================
-- 3. 任务历史记录表
-- ====================================
CREATE TABLE `task_history` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史记录ID',
    `task_id` BIGINT NOT NULL COMMENT '任务ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `action` VARCHAR(50) NOT NULL COMMENT '操作类型：CREATE-创建，UPDATE-更新，STATUS_CHANGE-状态变更，DELETE-删除',
    `old_value` JSON COMMENT '变更前的值',
    `new_value` JSON COMMENT '变更后的值',
    `field_name` VARCHAR(50) COMMENT '变更字段名',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_task_id (`task_id`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_action (`action`),
    INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务历史记录表';

-- ====================================
-- 4. 任务统计表（用于耗时预测和报告生成）
-- ====================================
CREATE TABLE `task_statistics` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category` VARCHAR(50) COMMENT '任务分类',
    `avg_duration` INT COMMENT '平均耗时（分钟）',
    `total_tasks` INT DEFAULT 0 COMMENT '总任务数',
    `completed_tasks` INT DEFAULT 0 COMMENT '已完成任务数',
    `delayed_tasks` INT DEFAULT 0 COMMENT '延期任务数',
    `completion_rate` DECIMAL(5,2) COMMENT '完成率（%）',
    `delay_rate` DECIMAL(5,2) COMMENT '延期率（%）',
    `stat_date` DATE NOT NULL COMMENT '统计日期',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_category_date (`user_id`, `category`, `stat_date`),
    INDEX idx_user_id (`user_id`),
    INDEX idx_stat_date (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务统计表';

-- ====================================
-- 5. 任务报告表
-- ====================================
CREATE TABLE `task_report` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报告ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `report_type` VARCHAR(20) NOT NULL COMMENT '报告类型：DAILY-日报，WEEKLY-周报，MONTHLY-月报',
    `title` VARCHAR(200) NOT NULL COMMENT '报告标题',
    `content` TEXT NOT NULL COMMENT '报告内容',
    `start_date` DATE NOT NULL COMMENT '统计开始日期',
    `end_date` DATE NOT NULL COMMENT '统计结束日期',
    `total_tasks` INT DEFAULT 0 COMMENT '总任务数',
    `completed_tasks` INT DEFAULT 0 COMMENT '已完成任务数',
    `total_duration` INT DEFAULT 0 COMMENT '总耗时（分钟）',
    `statistics` JSON COMMENT '详细统计数据（JSON格式）',
    `generated_by` VARCHAR(20) DEFAULT 'MANUAL' COMMENT '生成方式：MANUAL-手动，AUTO-自动，AI-AI生成',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_report_type (`report_type`),
    INDEX idx_start_date (`start_date`),
    INDEX idx_end_date (`end_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务报告表';

-- ====================================
-- 6. 任务模板表
-- ====================================
CREATE TABLE `task_template` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模板ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `title` VARCHAR(200) NOT NULL COMMENT '任务标题模板',
    `description` TEXT COMMENT '任务描述模板',
    `category` VARCHAR(50) COMMENT '任务分类',
    `estimated_duration` INT COMMENT '预估耗时（分钟）',
    `tags` JSON COMMENT '默认标签',
    `use_count` INT DEFAULT 0 COMMENT '使用次数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_category (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务模板表';

-- ====================================
-- 7. AI调用日志表（用于监控和优化）
-- ====================================
CREATE TABLE `ai_call_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `user_id` BIGINT COMMENT '用户ID',
    `function_name` VARCHAR(100) NOT NULL COMMENT '功能名称：NLP_PARSE-自然语言解析，PRIORITY_CALC-优先级计算，DURATION_PREDICT-耗时预测，REPORT_GEN-报告生成',
    `input_text` TEXT COMMENT '输入内容',
    `output_text` TEXT COMMENT '输出内容',
    `prompt` TEXT COMMENT '使用的Prompt',
    `model` VARCHAR(50) COMMENT 'AI模型名称',
    `tokens_used` INT COMMENT '消耗的Token数',
    `response_time` INT COMMENT '响应时间（毫秒）',
    `status` VARCHAR(20) DEFAULT 'SUCCESS' COMMENT '调用状态：SUCCESS-成功，FAILED-失败',
    `error_message` TEXT COMMENT '错误信息',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '调用时间',
    INDEX idx_user_id (`user_id`),
    INDEX idx_function_name (`function_name`),
    INDEX idx_status (`status`),
    INDEX idx_created_at (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';