package com.scu.ai_task_management.common.utils;

import com.scu.ai_task_management.infra.ai.model.TaskParseResponse;
import com.scu.ai_task_management.common.exception.AIException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.scu.ai_task_management.common.constants.AIConstants.*;

/**
 * 基于正则表达式的兜底任务解析策略
 * 支持丰富的中文自然语言时间表达
 */
public final class FallbackTaskUtil {


    private FallbackTaskUtil() {
    }

    /**
     * 解析自然语言输入为任务对象
     */
    public static TaskParseResponse parse(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw AIException.fallbackFailed();
        }

        String normalized = input.trim();
        String title = buildTitle(normalized);
        LocalDateTime deadline = extractDeadline(normalized);
        Integer duration = extractDuration(normalized);
        String status = inferStatus(normalized);
        String category = inferCategory(normalized);
        Integer priority = inferPriority(normalized, deadline);
        List<String> tags = extractTags(normalized, category);

        return TaskParseResponse.builder()
                .title(title)
                .description(normalized)
                .status(status)
                .category(category)
                .deadline(deadline)
                .estimatedDuration(duration)
                .tags(tags)
                .confidence(0.3)
                .build();
    }

    /**
     * 构建任务标题（取第一句或限制长度）
     */
    private static String buildTitle(String input) {
        // 移除标签和时间等信息
        String cleaned = input.replaceAll("#[\\w\\u4e00-\\u9fa5]+", "")
                .replaceAll("\\d{4}-\\d{2}-\\d{2}", "")
                .replaceAll("\\d{1,2}[点:]\\d{2}", "")
                .trim();

        String[] split = cleaned.split("[\\n。.!?；;]");
        String candidate = split.length > 0 ? split[0].trim() : cleaned;

        // 移除常见的时间词
        candidate = candidate.replaceAll("(今天|明天|后天|下周|上午|下午|早上|中午|晚上)", "").trim();

        return candidate.length() > 50 ? candidate.substring(0, 50) + "..." : candidate;
    }

    /**
     * 提取截止时间（支持多种自然语言表达）
     */
    private static LocalDateTime extractDeadline(String input) {
        // 1. 尝试匹配ISO格式日期时间
        Matcher isoMatcher = ISO_DATETIME_PATTERN.matcher(input);
        if (isoMatcher.find()) {
            return parseISODateTime(isoMatcher.group(1));
        }

        // 2. 匹配具体日期（如：12月25日）
        Matcher dateMatcher = DATE_PATTERN.matcher(input);
        if (dateMatcher.find()) {
            int month = Integer.parseInt(dateMatcher.group(1));
            int day = Integer.parseInt(dateMatcher.group(2));
            LocalDate date = LocalDate.of(LocalDate.now().getYear(), month, day);
            // 如果日期已过，则认为是明年
            if (date.isBefore(LocalDate.now())) {
                date = date.plusYears(1);
            }
            LocalTime time = extractTime(input);
            return LocalDateTime.of(date, time != null ? time : LocalTime.of(18, 0));
        }

        // 3. 匹配相对日期（今天、明天等）
        LocalDate baseDate = extractRelativeDate(input);

        // 4. 匹配星期（下周一等）
        if (baseDate == null) {
            baseDate = extractWeekday(input);
        }

        // 5. 提取时间
        LocalTime time = extractTime(input);

        // 6. 组合日期和时间
        if (baseDate != null) {
            return LocalDateTime.of(baseDate, time != null ? time : LocalTime.of(18, 0));
        }

        return null;
    }

    /**
     * 解析ISO格式日期时间
     */
    private static LocalDateTime parseISODateTime(String raw) {
        raw = raw.replace("T", " ");
        if (raw.length() == 10) {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    .atTime(LocalTime.of(18, 0));
        }
        DateTimeFormatter formatter;
        if (raw.length() == 16) {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        } else {
            formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        }
        return LocalDateTime.parse(raw, formatter);
    }

    /**
     * 提取相对日期（今天、明天、后天等）
     */
    private static LocalDate extractRelativeDate(String input) {
        for (Map.Entry<String, Integer> entry : RELATIVE_DAYS.entrySet()) {
            if (input.contains(entry.getKey())) {
                return LocalDate.now().plusDays(entry.getValue());
            }
        }

        // 匹配"N天后"
        Pattern daysLaterPattern = Pattern.compile("(\\d+)天[后之]?后");
        Matcher matcher = daysLaterPattern.matcher(input);
        if (matcher.find()) {
            int days = Integer.parseInt(matcher.group(1));
            return LocalDate.now().plusDays(days);
        }

        // 匹配"下周"、"下个月"
        if (input.contains("下周") || input.contains("下星期")) {
            return LocalDate.now().plusWeeks(1);
        }
        if (input.contains("下个月") || input.contains("下月")) {
            return LocalDate.now().plusMonths(1);
        }

        return null;
    }

    /**
     * 提取星期（周一到周日）
     */
    private static LocalDate extractWeekday(String input) {
        boolean isNext = input.contains("下周") || input.contains("下个") || input.contains("下星期");

        for (Map.Entry<String, DayOfWeek> entry : WEEKDAY_MAP.entrySet()) {
            if (input.contains(entry.getKey())) {
                LocalDate date = LocalDate.now().with(TemporalAdjusters.next(entry.getValue()));
                if (isNext) {
                    date = date.plusWeeks(1);
                }
                return date;
            }
        }
        return null;
    }

    /**
     * 提取时间（支持上午、下午等修饰）
     */
    private static LocalTime extractTime(String input) {
        Matcher timeMatcher = TIME_PATTERN.matcher(input);
        if (timeMatcher.find()) {
            String fullMatch = timeMatcher.group(0);
            int hour = Integer.parseInt(timeMatcher.group(1));
            String minuteGroup = timeMatcher.group(2);
            int minute = minuteGroup != null ? Integer.parseInt(minuteGroup) : 0;

            // 根据上午/下午调整小时
            if (fullMatch.contains("下午") || fullMatch.contains("晚上")) {
                if (hour < 12) hour += 12;
            } else if (fullMatch.contains("凌晨") || fullMatch.contains("早上")) {
                if (hour == 12) hour = 0;
            } else if (fullMatch.contains("中午")) {
                hour = 12;
            }

            return LocalTime.of(Math.min(hour, 23), Math.min(minute, 59));
        }
        return null;
    }

    /**
     * 提取预估时长
     */
    private static Integer extractDuration(String input) {
        Matcher matcher = DURATION_PATTERN.matcher(input);
        if (matcher.find()) {
            double value = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2);

            // 转换为分钟
            if ("小时".equals(unit) || "时".equals(unit) || "h".equalsIgnoreCase(unit) || unit.toLowerCase().startsWith("hour")) {
                return (int) (value * 60);
            } else if ("天".equals(unit) || "日".equals(unit) || unit.toLowerCase().startsWith("day")) {
                return (int) (value * 60 * 8); // 假设一天工作8小时
            }
            return (int) value;
        }
        return null;
    }

    /**
     * 推断任务状态
     */
    private static String inferStatus(String input) {
        if (input.matches(".*(完成|已完成|结束|done|finished|completed).*")) {
            return "COMPLETED";
        }
        if (input.matches(".*(进行中|正在|doing|processing|in progress).*")) {
            return "IN_PROGRESS";
        }
        if (input.matches(".*(取消|放弃|cancel|cancelled|abandoned).*")) {
            return "CANCELLED";
        }
        if (input.matches(".*(暂停|搁置|pending|paused).*")) {
            return "PENDING";
        }
        return "TODO";
    }

    /**
     * 推断任务分类
     */
    private static String inferCategory(String input) {
        if (input.matches(".*(学习|考试|作业|课程|复习|预习|论文|研究).*")) {
            return "学习";
        }
        if (input.matches(".*(会议|项目|开发|上线|需求|代码|测试|部署|评审).*")) {
            return "工作";
        }
        if (input.matches(".*(健身|跑步|锻炼|健康|运动|瑜伽|游泳).*")) {
            return "健康";
        }
        if (input.matches(".*(家庭|买菜|做饭|家务|打扫|购物|缴费).*")) {
            return "生活";
        }
        if (input.matches(".*(聚会|约会|电影|旅游|娱乐|游戏).*")) {
            return "娱乐";
        }
        return "其他";
    }

    /**
     * 推断优先级（0-100）
     */
    private static Integer inferPriority(String input, LocalDateTime deadline) {
        int priority = 50; // 基础优先级

        // 根据关键词调整
        if (input.matches(".*(紧急|urgent|asap|立即|马上).*")) {
            priority += 30;
        }
        if (input.matches(".*(重要|important|关键|核心).*")) {
            priority += 20;
        }
        if (input.matches(".*(可选|optional|不急|有空).*")) {
            priority -= 20;
        }

        // 根据截止时间调整
        if (deadline != null) {
            long hoursUntilDeadline = java.time.temporal.ChronoUnit.HOURS.between(LocalDateTime.now(), deadline);
            if (hoursUntilDeadline < 24) {
                priority += 20;
            } else if (hoursUntilDeadline < 72) {
                priority += 10;
            }
        }

        return Math.max(0, Math.min(100, priority));
    }

    /**
     * 提取标签
     */
    private static List<String> extractTags(String input, String category) {
        Set<String> tags = new LinkedHashSet<>();

        // 提取#标签
        Matcher matcher = HASH_TAG_PATTERN.matcher(input);
        while (matcher.find()) {
            tags.add(matcher.group(1));
        }

        // 自动添加关键词标签
        if (input.contains("紧急") || input.contains("urgent")) {
            tags.add("紧急");
        }
        if (input.contains("重要") || input.contains("important")) {
            tags.add("重要");
        }
        if (input.contains("团队") || input.contains("协作")) {
            tags.add("团队协作");
        }
        if (input.contains("个人")) {
            tags.add("个人");
        }

        // 添加分类标签
        if (category != null && !"其他".equals(category)) {
            tags.add(category);
        }

        return new ArrayList<>(tags);
    }

    static {
        RELATIVE_DAYS.put("今天", 0);
        RELATIVE_DAYS.put("今日", 0);
        RELATIVE_DAYS.put("明天", 1);
        RELATIVE_DAYS.put("明日", 1);
        RELATIVE_DAYS.put("后天", 2);
        RELATIVE_DAYS.put("大后天", 3);

        WEEKDAY_MAP.put("周一", DayOfWeek.MONDAY);
        WEEKDAY_MAP.put("周二", DayOfWeek.TUESDAY);
        WEEKDAY_MAP.put("周三", DayOfWeek.WEDNESDAY);
        WEEKDAY_MAP.put("周四", DayOfWeek.THURSDAY);
        WEEKDAY_MAP.put("周五", DayOfWeek.FRIDAY);
        WEEKDAY_MAP.put("周六", DayOfWeek.SATURDAY);
        WEEKDAY_MAP.put("周日", DayOfWeek.SUNDAY);
        WEEKDAY_MAP.put("星期一", DayOfWeek.MONDAY);
        WEEKDAY_MAP.put("星期二", DayOfWeek.TUESDAY);
        WEEKDAY_MAP.put("星期三", DayOfWeek.WEDNESDAY);
        WEEKDAY_MAP.put("星期四", DayOfWeek.THURSDAY);
        WEEKDAY_MAP.put("星期五", DayOfWeek.FRIDAY);
        WEEKDAY_MAP.put("星期六", DayOfWeek.SATURDAY);
        WEEKDAY_MAP.put("星期日", DayOfWeek.SUNDAY);
    }
}
