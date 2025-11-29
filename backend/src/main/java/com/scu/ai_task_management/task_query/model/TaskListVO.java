package com.scu.ai_task_management.task_query.model;

import com.scu.ai_task_management.task_basic.model.TaskVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 任务列表VO（分页返回）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "任务列表（分页）")
public class TaskListVO {

    @Schema(description = "任务列表")
    private List<TaskVO> tasks;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "当前页码（从0开始）", example = "0")
    private Integer page;

    @Schema(description = "每页数量", example = "10")
    private Integer size;

    @Schema(description = "总页数", example = "10")
    private Integer totalPages;
}
