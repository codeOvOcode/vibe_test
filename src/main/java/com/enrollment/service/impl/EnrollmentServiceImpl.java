/*
 * ============================================================
 * 代码来源标注
 * ============================================================
 * AI工具: Sisyphus (DeepSeek-V4-Pro)
 * AI生成比例: 85%
 * AI生成部分: CSV解析（split按行/逗号）、Excel解析（Apache POI）、
 *             LinkedHashMap去重、stream排序、classify分类Map、
 *             search switch路由、importCsv/importExcel管道拼接（含DB持久化）、
 *             getSampleData/getByType、ensureStudentExists/CourseExists自动补全、
 *             Excel导出（Apache POI含样式）
 * 手动修改: 中文错误提示（"CSV数据不能为空"、"第X行字段不足"），
 *           字段值添加trim()处理空白字符，DB持久化逻辑优化
 * 修改原因: 中文化错误提示方便操作人员理解，
 *           trim()提高CSV数据兼容性，避免空格导致匹配失败
 * 详见: AI提示词与代码标注.md → 提示词二
 * ============================================================
 */
package com.enrollment.service.impl;

import com.enrollment.dto.CourseEnrollmentVO;
import com.enrollment.dto.ImportResult;
import com.enrollment.entity.Course;
import com.enrollment.entity.Enrollment;
import com.enrollment.entity.Student;
import com.enrollment.repository.CourseRepository;
import com.enrollment.repository.EnrollmentRepository;
import com.enrollment.repository.StudentRepository;
import com.enrollment.service.EnrollmentService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public EnrollmentServiceImpl(EnrollmentRepository enrollmentRepository,
                                  CourseRepository courseRepository,
                                  StudentRepository studentRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
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
    public List<CourseEnrollmentVO> parseExcel(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Excel文件不能为空");
        }
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("仅支持 .xlsx 格式的Excel文件");
        }
        List<CourseEnrollmentVO> records = new ArrayList<>();
        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                int emptyCount = 0;
                for (int j = 0; j < 4; j++) {
                    Cell cell = row.getCell(j);
                    if (cell == null || getCellString(cell).isBlank()) emptyCount++;
                }
                if (emptyCount == 4) continue;

                String sid = getCellString(row.getCell(0));
                String cid = getCellString(row.getCell(1));
                String name = getCellString(row.getCell(2));
                String type = getCellString(row.getCell(3));

                if (sid.isBlank() || cid.isBlank() || name.isBlank() || type.isBlank()) {
                    throw new IllegalArgumentException(
                        String.format("Excel第%d行数据不完整，需要4列（学生ID,课程ID,课程名称,课程类型）", i + 1));
                }
                records.add(new CourseEnrollmentVO(sid, cid, name, type));
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Excel文件解析失败: " + e.getMessage(), e);
        }
        return records;
    }

    private String getCellString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double v = cell.getNumericCellValue();
                yield v == Math.floor(v) ? String.valueOf((long) v) : String.valueOf(v);
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
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
        return persistAndBuildResult(parsed);
    }

    @Override
    public ImportResult importExcel(MultipartFile file) {
        List<CourseEnrollmentVO> parsed = parseExcel(file);
        return persistAndBuildResult(parsed);
    }

    private ImportResult persistAndBuildResult(List<CourseEnrollmentVO> parsed) {
        int originalSize = parsed.size();
        List<CourseEnrollmentVO> deduped = deduplicate(parsed);
        int duplicated = originalSize - deduped.size();

        int saved = 0;
        int skipped = 0;
        for (CourseEnrollmentVO vo : deduped) {
            try {
                ensureStudentExists(vo.getStudentId());
                ensureCourseExists(vo.getCourseId(), vo.getCourseName(), vo.getCourseType());

                Enrollment.EnrollmentId id = new Enrollment.EnrollmentId(vo.getStudentId(), vo.getCourseId());
                if (enrollmentRepository.existsById(id)) {
                    skipped++;
                    continue;
                }

                Enrollment enrollment = new Enrollment();
                enrollment.setStudentId(vo.getStudentId());
                enrollment.setCourseId(vo.getCourseId());
                enrollment.setEnrollTime(LocalDateTime.now());
                enrollment.setStatus("正常");
                enrollmentRepository.save(enrollment);
                saved++;
            } catch (DataIntegrityViolationException e) {
                skipped++;
            }
        }

        List<CourseEnrollmentVO> allData = getAllData();
        List<CourseEnrollmentVO> sorted = sort(allData);
        Map<String, List<CourseEnrollmentVO>> classified = classify(sorted);

        ImportResult result = new ImportResult();
        result.setTotal(saved);
        result.setDuplicated(duplicated + skipped);
        result.setRecords(sorted);
        result.setClassified(classified);
        return result;
    }

    private void ensureStudentExists(String studentId) {
        if (!studentRepository.existsById(studentId)) {
            Student s = new Student(studentId, studentId);
            studentRepository.save(s);
        }
    }

    private void ensureCourseExists(String courseId, String courseName, String courseType) {
        if (!courseRepository.existsById(courseId)) {
            Course c = new Course();
            c.setCourseId(courseId);
            c.setCourseName(courseName);
            c.setCourseType(courseType);
            c.setCapacity(30);
            c.setCredits(2);
            courseRepository.save(c);
        }
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

    @Override
    public byte[] exportExcel() {
        List<CourseEnrollmentVO> records = getAllData();
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("选课记录");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"学生ID", "课程ID", "课程名称", "课程类型"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            int rowIdx = 1;
            for (CourseEnrollmentVO vo : records) {
                Row row = sheet.createRow(rowIdx++);
                Cell c0 = row.createCell(0); c0.setCellValue(vo.getStudentId()); c0.setCellStyle(dataStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(vo.getCourseId()); c1.setCellStyle(dataStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(vo.getCourseName()); c2.setCellStyle(dataStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(vo.getCourseType()); c3.setCellStyle(dataStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }
}
