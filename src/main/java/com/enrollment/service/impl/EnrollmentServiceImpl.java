package com.enrollment.service.impl;

import com.enrollment.dto.CourseEnrollmentVO;
import com.enrollment.dto.ImportResult;
import com.enrollment.repository.EnrollmentRepository;
import com.enrollment.service.EnrollmentService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    private static final Map<String, List<CourseEnrollmentVO>> inMemoryStore = new LinkedHashMap<>();

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    @Override
    public List<CourseEnrollmentVO> parseCsv(String csvData) {
        if (csvData == null || csvData.isBlank()) {
            throw new IllegalArgumentException("CSV数据不能为空");
        }
        List<CourseEnrollmentVO> records = new ArrayList<>();
        String[] lines = csvData.split("\\r?\\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 4) {
                throw new IllegalArgumentException(
                    String.format("CSV格式错误：第%d行字段不足，需要4个字段（学生ID,课程ID,课程名称,课程类型）", i + 1));
            }
            records.add(new CourseEnrollmentVO(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim()
            ));
        }
        return records;
    }

    @Override
    public List<CourseEnrollmentVO> deduplicate(List<CourseEnrollmentVO> records) {
        if (records == null) throw new IllegalArgumentException("记录列表不能为空");
        Map<String, CourseEnrollmentVO> seen = new LinkedHashMap<>();
        for (CourseEnrollmentVO r : records) {
            String key = r.getStudentId() + "|" + r.getCourseId();
            seen.putIfAbsent(key, r);
        }
        return new ArrayList<>(seen.values());
    }

    @Override
    public List<CourseEnrollmentVO> sort(List<CourseEnrollmentVO> records) {
        if (records == null) throw new IllegalArgumentException("记录列表不能为空");
        return records.stream()
                .sorted(Comparator.comparing(CourseEnrollmentVO::getStudentId)
                        .thenComparing(CourseEnrollmentVO::getCourseId))
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<CourseEnrollmentVO>> classify(List<CourseEnrollmentVO> records) {
        Map<String, List<CourseEnrollmentVO>> result = new LinkedHashMap<>();
        result.put("专业课", new ArrayList<>());
        result.put("公共课", new ArrayList<>());
        result.put("选修课", new ArrayList<>());

        for (CourseEnrollmentVO r : records) {
            String type = r.getCourseType();
            result.computeIfAbsent(type, k -> new ArrayList<>()).add(r);
        }
        return result;
    }

    @Override
    public List<CourseEnrollmentVO> search(String keyword, String type) {
        if (keyword == null || keyword.isBlank()) {
            return getAllData();
        }
        return switch (type) {
            case "studentId" -> enrollmentRepository.searchByStudentId(keyword.trim());
            case "courseId" -> enrollmentRepository.searchByCourseId(keyword.trim());
            case "courseName" -> enrollmentRepository.searchByCourseName(keyword.trim());
            case "courseType" -> enrollmentRepository.searchByCourseType(keyword.trim());
            default -> getAllData();
        };
    }

    @Override
    public ImportResult importCsv(String csvData) {
        List<CourseEnrollmentVO> parsed = parseCsv(csvData);
        int originalSize = parsed.size();
        List<CourseEnrollmentVO> deduped = deduplicate(parsed);
        int duplicated = originalSize - deduped.size();
        List<CourseEnrollmentVO> sorted = sort(deduped);
        Map<String, List<CourseEnrollmentVO>> classified = classify(sorted);

        ImportResult result = new ImportResult();
        result.setTotal(sorted.size());
        result.setDuplicated(duplicated);
        result.setRecords(sorted);
        result.setClassified(classified);
        return result;
    }

    @Override
    public List<CourseEnrollmentVO> getSampleData() {
        return enrollmentRepository.findAllEnrollments();
    }

    @Override
    public Map<String, List<CourseEnrollmentVO>> getByType(String courseType) {
        List<CourseEnrollmentVO> all = getAllData();
        if (courseType != null && !courseType.isBlank()) {
            List<CourseEnrollmentVO> filtered = all.stream()
                    .filter(r -> courseType.equals(r.getCourseType()))
                    .collect(Collectors.toList());
            Map<String, List<CourseEnrollmentVO>> result = new LinkedHashMap<>();
            result.put(courseType, filtered);
            return result;
        }
        return classify(all);
    }

    @Override
    public List<CourseEnrollmentVO> getByTypeFlat(String courseType) {
        List<CourseEnrollmentVO> all = getAllData();
        if (courseType == null || courseType.isBlank()) {
            return all;
        }
        return all.stream()
                .filter(r -> courseType.equals(r.getCourseType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CourseEnrollmentVO> getAllData() {
        return enrollmentRepository.findAllEnrollments();
    }
}
