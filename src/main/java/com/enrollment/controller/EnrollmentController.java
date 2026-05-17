package com.enrollment.controller;

import com.enrollment.dto.ApiResponse;
import com.enrollment.dto.CourseEnrollmentVO;
import com.enrollment.dto.ImportResult;
import com.enrollment.repository.CourseRepository;
import com.enrollment.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseRepository courseRepository;

    public EnrollmentController(EnrollmentService enrollmentService, CourseRepository courseRepository) {
        this.enrollmentService = enrollmentService;
        this.courseRepository = courseRepository;
    }

    @PostMapping("/enrollment/import")
    public ResponseEntity<ApiResponse<ImportResult>> importCsv(@RequestParam String csvData) {
        try {
            ImportResult result = enrollmentService.importCsv(csvData);
            String msg = String.format("导入成功，共处理 %d 条记录（去重 %d 条）", result.getTotal(), result.getDuplicated());
            return ResponseEntity.ok(ApiResponse.success(msg, result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/enrollment/search")
    public ResponseEntity<ApiResponse<List<CourseEnrollmentVO>>> search(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "courseName") String type) {
        List<CourseEnrollmentVO> results = enrollmentService.search(keyword, type);
        if (results.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("无匹配选课记录", List.of()));
        }
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/enrollment/sample")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSampleData() {
        List<CourseEnrollmentVO> records = enrollmentService.getSampleData();
        List<CourseEnrollmentVO> sorted = enrollmentService.sort(records);
        Map<String, List<CourseEnrollmentVO>> classified = enrollmentService.classify(sorted);

        Map<String, Object> data = Map.of(
            "records", sorted,
            "classified", classified
        );
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    @GetMapping("/enrollment/by-type")
    public ResponseEntity<ApiResponse<Map<String, List<CourseEnrollmentVO>>>> getByType(
            @RequestParam(required = false) String courseType) {
        Map<String, List<CourseEnrollmentVO>> result = enrollmentService.getByType(courseType);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<?>>> getCourses() {
        return ResponseEntity.ok(ApiResponse.success(courseRepository.findAll()));
    }
}
