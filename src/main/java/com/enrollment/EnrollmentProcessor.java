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
