/*
 * ============================================================
 * 代码来源标注
 * ============================================================
 * AI工具: Sisyphus (DeepSeek-V4-Pro)
 * AI生成比例: 90%
 * AI生成部分: @RestController/@RequestMapping注解、构造器注入、
 *             六个REST接口方法签名、ApiResponse统一封装、
 *             ResponseEntity返回包装、Excel导出端点
 * 手动修改: "无匹配选课记录"中文提示、导入成功统计消息格式
 *           （"导入成功，持久化 X 条记录（去重/跳过 Y 条）"）
 * 修改原因: 适配选课场景，中文化提示信息，
 *           导入反馈需显示处理条数和去重条数便于用户确认
 * 详见: AI提示词与代码标注.md → 提示词二
 * ============================================================
 */
package com.enrollment.controller;

import com.enrollment.dto.ApiResponse;
import com.enrollment.dto.CourseEnrollmentVO;
import com.enrollment.dto.ImportResult;
import com.enrollment.repository.CourseRepository;
import com.enrollment.service.EnrollmentService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            String msg = String.format("导入成功，持久化 %d 条记录（去重/跳过 %d 条）", result.getTotal(), result.getDuplicated());
            return ResponseEntity.ok(ApiResponse.success(msg, result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @PostMapping("/enrollment/import-excel")
    public ResponseEntity<ApiResponse<ImportResult>> importExcel(@RequestParam MultipartFile file) {
        try {
            ImportResult result = enrollmentService.importExcel(file);
            String msg = String.format("Excel导入成功，持久化 %d 条记录（去重/跳过 %d 条）", result.getTotal(), result.getDuplicated());
            return ResponseEntity.ok(ApiResponse.success(msg, result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, e.getMessage()));
        }
    }

    @GetMapping("/enrollment/export")
    public ResponseEntity<byte[]> exportExcel() {
        byte[] excelBytes = enrollmentService.exportExcel();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filename = "选课记录_" + timestamp + ".xlsx";
        String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFilename)
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excelBytes);
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
