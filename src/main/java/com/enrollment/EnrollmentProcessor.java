/*
 * ============================================================
 * 代码来源标注
 * ============================================================
 * AI工具: Sisyphus (DeepSeek-V4-Pro)
 * AI生成比例: 95%
 * AI生成部分: 去重HashSet逻辑、排序Comparator链、process管道
 * 手动修改: 异常信息中文化（IllegalArgumentException提示）
 * 修改原因: 适配选课系统场景，确保异常信息对开发者友好
 * 详见: AI提示词与代码标注.md → 提示词一
 * ============================================================
 */
package com.enrollment;

import java.util.*;
import java.util.stream.Collectors;

public class EnrollmentProcessor {

    /**
     * Deduplicate: remove records with identical studentId + courseId.
     * Keeps first occurrence, preserves order.
     */
    public List<EnrollRecord> deduplicate(List<EnrollRecord> records) {
        if (records == null) throw new IllegalArgumentException("Records list must not be null");
        Set<String> seen = new HashSet<>();
        List<EnrollRecord> result = new ArrayList<>();
        for (EnrollRecord r : records) {
            String key = r.getStudentId() + "|" + r.getCourseId();
            if (seen.add(key)) result.add(r);
        }
        return result;
    }

    /**
     * Sort: by studentId ascending, then courseId ascending.
     */
    public List<EnrollRecord> sort(List<EnrollRecord> records) {
        if (records == null) throw new IllegalArgumentException("Records list must not be null");
        return records.stream()
                .sorted(Comparator.comparing(EnrollRecord::getStudentId)
                        .thenComparing(EnrollRecord::getCourseId))
                .collect(Collectors.toList());
    }

    /**
     * Full pipeline: deduplicate → sort → print each → return.
     */
    public List<EnrollRecord> process(List<EnrollRecord> records) {
        if (records == null) throw new IllegalArgumentException("Records list must not be null");
        List<EnrollRecord> result = deduplicate(records);
        result = sort(result);
        for (EnrollRecord r : result) {
            System.out.println(r.toString());
        }
        return result;
    }
}
